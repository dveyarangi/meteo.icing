################################################
# This file writes RH and temperature probability
# files for each date, so that no calculations
# are needed for these fielda in future use.
# The files were written using the
# NETCDF3_64BIT_OFFSET format to fit  the other
# downloaded NetCDF files, but that means that
# the compression flag (zlib) is ignored. For
# future use, use NetCDF4 to allow compression.
################################################

import os

from netCDF4 import Dataset, date2num

from archive.structure import MDataset
from util.constants import *

# open archive catalogue for getting the time
isobaric = MDataset("isobaric")
t_var = isobaric['t']
rh_var = isobaric['rh']

################################################
# get working grid dimensions:
grid_width = t_var.shape[2]
grid_height = t_var.shape[3]

# for current_time in range(0,len(isobaric.times)+1):
for current_time in range(0, len(isobaric.times) + 1):
    # Get the time in a datetime object
    current_date = isobaric.times[current_time]

    # Create the file itself for the RHP part
    datestr = current_date.strftime('%Y-%m-%d_%H')
    print (datestr)
    print (current_time)
    directory = 'ERA_Interim/' + datestr[0:4] + '/' + datestr[0:7] + '/' + datestr[8:10] + '/'
    if not os.path.exists(directory):
        os.makedirs(directory)
    filename = directory + datestr + 'Z_RHP_isobaric.nc'
    rootgrp = Dataset(filename, "w", format="NETCDF3_64BIT_OFFSET")
    # rootgrp = Dataset(filename, "w", format="NETCDF4") # If compression is needed

    # Create the file's dimensions
    time = rootgrp.createDimension("time", 1)
    level = rootgrp.createDimension("level", 19)
    latitude = rootgrp.createDimension("latitude", 62)
    longitude = rootgrp.createDimension("longitude", 108)

    # Create the file's variables (zlib = True works with NETCDF4 file format and is ignored in NETCDF3_64BIT_OFFSET")
    times = rootgrp.createVariable("time", np.float64, ("time",), zlib=True)
    levels = rootgrp.createVariable("level", np.int32, ("level",), zlib=True)
    latitudes = rootgrp.createVariable("latitude", np.float32, ("latitude",), zlib=True)
    longitudes = rootgrp.createVariable("longitude", np.float32, ("longitude",), zlib=True)
    rhp = rootgrp.createVariable("rhp", np.float32, ("time", "level", "latitude", "longitude",), zlib=True, complevel=9, least_significant_digit=3)

    # Write global attributes
    rootgrp.description = "RHP is the probability for icing base on RH only, taken from a pre-published probabilities table"
    rootgrp.source = 'The probability table can be found in util/constants.py'

    # Write variables' attributes
    latitudes.units = 'degree_north'
    longitudes.units = 'degree_east'
    levels.units = 'hPa'
    rhp.units = 'percents'
    times.units = 'hours since 1900-01-01 00:00:00'
    times.calendar = 'gregorian'

    # Write data into variables
    lats = np.arange(50.25, 3.75, -0.75)
    lons = np.arange(-5.25, 75.75, 0.75)
    latitudes[:] = lats
    longitudes[:] = lons
    levels[:] = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
    times[0] = date2num(current_date, times.units, times.calendar)

    rhp_grid = np.ones([t_var.shape[1], t_var.shape[2], t_var.shape[3]])
    tp_grid = np.ones([t_var.shape[1], t_var.shape[2], t_var.shape[3]])
    for current_level in range(19):
        rh_level = np.array(rh_var[current_date][current_level])
        t_level = np.array(t_var[current_date][current_level])
        for width_idx in range(grid_width):
            for height_idx in range(grid_height):
                RH_index = (np.abs(RH_probabilities[:, 0] - rh_level[width_idx, height_idx])).argmin()
                RH_value = RH_probabilities[RH_index, 1]
                temperature_index = (np.abs(temperature_probabilities[:, 0] - t_level[width_idx, height_idx] + ZERO_K)).argmin()
                temperature_value = temperature_probabilities[temperature_index, 1]
                tp_grid[current_level, width_idx, height_idx] = temperature_value
                rhp_grid[current_level, width_idx, height_idx] = RH_value

    rhp[:] = rhp_grid
    # Close the file
    rootgrp.close()

    # Create the file itself for the TP part
    filename = directory + datestr + 'Z_TP_isobaric.nc'
    rootgrp = Dataset(filename, "w", format="NETCDF3_64BIT_OFFSET")
    # rootgrp = Dataset(filename, "w", format="NETCDF4") # If compression is needed

    # Create the file's dimensions
    time = rootgrp.createDimension("time", 1)
    level = rootgrp.createDimension("level", 19)
    latitude = rootgrp.createDimension("latitude", 62)
    longitude = rootgrp.createDimension("longitude", 108)

    # Create the file's variables (zlib = True works with NETCDF4 file format and is ignored in NETCDF3_64BIT_OFFSET")
    times = rootgrp.createVariable("time", np.float64, ("time",), zlib=True)
    levels = rootgrp.createVariable("level", np.int32, ("level",), zlib=True)
    latitudes = rootgrp.createVariable("latitude", np.float32, ("latitude",), zlib=True)
    longitudes = rootgrp.createVariable("longitude", np.float32, ("longitude",), zlib=True)
    tp = rootgrp.createVariable("tp", np.float32, ("time", "level", "latitude", "longitude",), zlib=True, complevel=9, least_significant_digit=2)

    # Write global attributes
    rootgrp.description = "TP is the probability for icing base on temperature only, taken from a pre-published probabilities table"
    rootgrp.source = 'The probability table can be found in util/constants.py'

    # Write variables' attributes
    latitudes.units = 'degree_north'
    longitudes.units = 'degree_east'
    levels.units = 'hPa'
    tp.units = 'percents'
    times.units = 'hours since 1900-01-01 00:00:00'
    times.calendar = 'gregorian'

    # Write data into variables
    lats = np.arange(50.25, 3.75, -0.75)
    lons = np.arange(-5.25, 75.75, 0.75)
    latitudes[:] = lats
    longitudes[:] = lons
    levels[:] = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
    times[0] = date2num(current_date, times.units, times.calendar)
    tp[:] = tp_grid

    # Close the file
    rootgrp.close()

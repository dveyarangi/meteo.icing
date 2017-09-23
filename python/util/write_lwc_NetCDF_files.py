################################################
# This file writes an LWC file for each date,
# so that no calculations are needed for this
# field in future use. The files were written
# using the NETCDF3_64BIT_OFFSET format to fit
# the other downloaded NetCDF files, but that
# means that the compression flag (zlib) is
# ignored. For future use, use NetCDF4 to allow
# compression.
################################################

from netCDF4 import Dataset, num2date, date2num
import numpy as np
from util.calculate_lwc import calculate_lwc
from archive.structure import MDataset
import os

# open archive catalogue for getting the time
isobaric = MDataset("isobaric")

#for current_time in range(0,len(isobaric.times)+1):
for current_time in range(6830, len(isobaric.times) + 1):
    # Get the time in a datetime object
    current_date = isobaric.times[current_time]

    # Create the file itself
    datestr = current_date.strftime('%Y-%m-%d_%H')
    print datestr
    print current_time
    directory = 'ERA_Interim/' + datestr[0:4] + '/' + datestr[0:7] + '/' + datestr[8:10] + '/'
    if not os.path.exists(directory):
        os.makedirs(directory)
    filename = directory + datestr + 'Z_LWC_isobaric.nc'
    rootgrp = Dataset(filename, "w", format="NETCDF3_64BIT_OFFSET")
    # rootgrp = Dataset(filename, "w", format="NETCDF4") # If compression is needed

    # Create the file's dimensions
    time = rootgrp.createDimension("time", 1)
    level = rootgrp.createDimension("level", 19)
    latitude = rootgrp.createDimension("latitude", 62)
    longitude = rootgrp.createDimension("longitude", 108)

    # Create the file's variables (zlib = True works with NETCDF4 file format and is ignored in NETCDF3_64BIT_OFFSET")
    times = rootgrp.createVariable("time", np.float64, ("time",),zlib=True)
    levels = rootgrp.createVariable("level", np.int32, ("level",),zlib=True)
    latitudes = rootgrp.createVariable("latitude", np.float32, ("latitude",),zlib=True)
    longitudes = rootgrp.createVariable("longitude", np.float32, ("longitude",),zlib=True)
    lwc = rootgrp.createVariable("lwc", np.float64, ("time", "level", "latitude", "longitude",),zlib=True,complevel=9)

    # Write global attributes
    rootgrp.description = "LWC is the liquid water content given in mass to volume (Kg/m3), calculated from the original ECMWF dataset for SLWC which is given in mass to mass (Kg/Kg) "
    rootgrp.source = 'Calculated by the script util/calculate_lwc.py'

    # Write variables' attributes
    latitudes.units = 'degree_north'
    longitudes.units = 'degree_east'
    levels.units = 'hPa'
    lwc.units = 'Kg/m3'
    times.units = 'hours since 1900-01-01 00:00:00'
    times.calendar = 'gregorian'

    # Write data into variables
    lats = np.arange(50.25, 3.75, -0.75)
    lons = np.arange(-5.25, 75.75, 0.75)
    latitudes[:] = lats
    longitudes[:] = lons
    levels[:] = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
    times[0] = date2num(current_date, times.units, times.calendar)
    lwc[:] = calculate_lwc(current_date)

    # Close the file
    rootgrp.close()

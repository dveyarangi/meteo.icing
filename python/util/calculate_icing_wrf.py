##################################################
# This file returns the icing probabilities
# values transformed from LWC and other parameters
# for a given date.
##################################################

import numpy as np
import sys
from wrf import getvar, interplevel, smooth2d, to_np, get_basemap, latlon_coords

from util.constants import *


def calculate_icing_wrf(ncfile, lwc, lwc_threshold):

    # levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
    #              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
    levels_list = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
    total_levels = np.size(levels_list, 0)

    ################################################
    # get temperature and relative humidity
    ################################################
    temperature_var = getvar(ncfile, "tc")
    rh_var = getvar(ncfile, "rh")

    # Get the pressure for interpolating the variables to the correct pressure level
    p = getvar(ncfile, "pressure")

    ################################################
    # calculate icing probabilities values
    ################################################
    grid_lats = rh_var.shape[1]
    grid_lons = rh_var.shape[2]
    probability_grid = np.ones([total_levels, grid_lats, grid_lons])

    for current_level in range(total_levels):
        # Interpolate the variables for the current pressure level
        current_pressure = levels_list[current_level]
        temperature_level = np.array(interplevel(temperature_var, p, current_pressure))
        rh_level = np.array(interplevel(rh_var, p, current_pressure))

        # Make the lwc values binary: 0 for less than the threshold and 1 for above it
        lwc_level = np.squeeze(lwc[current_level, :, :])
        lwc_level = np.ceil(lwc_level - lwc_threshold)

        for lats_idx in range(grid_lats):
            for lons_idx in range(grid_lons):
                RH_index = (np.abs(RH_probabilities[:, 0] - rh_level[lats_idx, lons_idx])).argmin()
                RH_value = RH_probabilities[RH_index, 1]
                temperature_index = (np.abs(temperature_probabilities[:, 0] - temperature_level[lats_idx, lons_idx])).argmin()
                temperature_value = temperature_probabilities[temperature_index, 1]
                probability_grid[current_level, lats_idx, lons_idx] = lwc_level[lats_idx, lons_idx] * temperature_value * RH_value

    return probability_grid



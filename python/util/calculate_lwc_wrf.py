################################################
# This file returns the LWC values transformed
# from SLWC for a given date
################################################

import numpy as np
from wrf import getvar, interplevel



def calculate_lwc_wrf(ncfile):

    # levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
    #              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
    levels_list = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
    total_levels = np.size(levels_list, 0)

    ################################################
    # get Virtual temperature, and CLWC.
    # In the ecmwf version we needed specific humidity and temperature to
    # calculate the virtual temperature. in WRF it is already calculated!
    ################################################
    virtual_temperature_var = getvar(ncfile, "tv")
    clwc_var = getvar(ncfile, "QCLOUD")

    # Get the pressure for interpolating the variables to the correct pressure level
    p = getvar(ncfile, "pressure")

    ################################################
    # calculate liquid water content in g/m3 values
    ################################################
    lwc_grid = np.ones([total_levels, clwc_var.shape[1], clwc_var.shape[2]])

    Rd = 2.87/1000  # m3*mb/Kg*K ->  m3*mb/g*K
    for current_level in range(total_levels):
        current_pressure = levels_list[current_level]

        # Interpolate the variables for the current pressure level
        virtual_temperature_grid = np.array(interplevel(virtual_temperature_var, p, current_pressure))
        clwc_grid = np.array(interplevel(clwc_var, p, current_pressure))

        # Calculate LWC
        air_density_grid = current_pressure / (Rd * virtual_temperature_grid)
        lwc_grid[current_level] = clwc_grid * air_density_grid

        ################################################
        # Debug messages
        # print("Current Level: " + str(current_level))
        # print("Max Air Density: " + str(air_density_grid[current_level].max()))
        # print("Max CLWC: " + str(clwc_grid.max()))
        # print("Max LWC: " + str(lwc_grid[current_level].max()))
        # print"========================================="
        ################################################

    return lwc_grid

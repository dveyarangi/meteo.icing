################################################
# This file returns the LWC values transformed
# from SLWC for a given date
################################################

import numpy as np
import sys

from archive.structure import MDataset


def calculate_lwc(req_date):
    ################################################
    # open archive catalogue:
    isobaric = MDataset("isobaric")

    # levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
    #              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
    level = np.array(range(19))

    ################################################
    # get SHUM grid, Temperature, CLWC and RH grids:
    q_var = isobaric['q']
    t_var = isobaric['t']
    clwc_var = isobaric['clwc']
    ################################################
    # calculate liquid water content in g/m3 values
    #lwc_grid = np.array([[], [], []])
    lwc_grid = np.ones([q_var.shape[1], q_var.shape[2], q_var.shape[3]])
    #lwc_grid.resize(q_var.shape[1], q_var.shape[2], q_var.shape[3])

    Rd = 2.87/1000  # m3*mb/Kg*K ->  m3*mb/g*K
    epsilon = 0.622  # Rd/Rv approximately in Earth's atmosphere
    for current_level in range(level[0], level[-1] + 1):
        pressure = isobaric.levels[current_level]# Not to do the following because Rd is in mb (hPa) * 100  # Hectopascal to Pascal

        if (q_var is None) or (t_var is None) or (clwc_var is None):
            print ("Failed to open sample grid")
            sys.exit()
        else:
            q_grid = np.array(q_var[req_date][current_level])
            t_grid = np.array(t_var[req_date][current_level])
            clwc_grid = np.array(clwc_var[req_date][current_level])

        omega_grid = q_grid / (1 - q_grid)
        virtual_t_grid = t_grid * (epsilon + omega_grid) / (epsilon * (1 + omega_grid))
        air_density_grid = pressure / (Rd * virtual_t_grid)
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

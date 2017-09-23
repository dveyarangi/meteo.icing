################################################
# This function returns icing probability maps in
# which the existance of a cloud is decided by
# an SLWC value higher than a threshold, and
# RH multiplied by air temperature is the probability.
################################################

import numpy as np
from netCDF4 import Dataset

from util.constants import *
import display_maps_GUI.constants_display_maps_GUI as const_GUI


def calculate_icing_probability(data_type, cross_section, hPa_level, cs_latlon, slwc_threshold,
                                files_path, req_year, req_month, req_day, req_hour):

    # levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
    #              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
    if cross_section == const_GUI.cross_section_1:
        # single geopotential level
        level = [str(x) for x in range(100, 1001, 50)].index(hPa_level)
    else:
        # longitude slice or latitude slice
        level = np.array(range(19))

    # Read the file
    directory_pre = files_path + '/' + req_year + '/' + req_year + '-' + req_month + '/' + req_day + '/' \
        + req_year + '-' + req_month + '-' + req_day + '_' + req_hour + 'Z_'
    directory_post = '_isobaric.nc'

    lwc_nc = Dataset(directory_pre + 'LWC' + directory_post)
    lats = lwc_nc.variables['latitude'][:]
    lons = lwc_nc.variables['longitude'][:]
    levels = lwc_nc.variables['level'][:]
    req_data = []
    vmin = []
    vmax = []

    if data_type == const_GUI.data_type_1:
        # Icing Probability
        lwc_data = lwc_nc.variables['lwc'][:]

        req_data = np.array([[], [], [], []])
        req_data.resize(1, levels.shape[0], lats.shape[0], lons.shape[0], refcheck=False)

        rhp_nc = Dataset(directory_pre + 'RHP' + directory_post)
        rhp_data = rhp_nc.variables['rhp'][:]

        tp_nc = Dataset(directory_pre + 'TP' + directory_post)
        tp_data = tp_nc.variables['tp'][:]

        for current_level in range(levels.shape[0]):
            lwc_level = np.squeeze(lwc_data[0, current_level, :, :])
            lwc_level = np.ceil(lwc_level - slwc_threshold)
            rhp_level = np.squeeze(rhp_data[0, current_level, :, :])
            tp_level = np.squeeze(tp_data[0, current_level, :, :])

            req_data[0, current_level, :, :] = rhp_level * tp_level * lwc_level

        vmin = 0
        vmax = 1

        ################################################
        # Debug info:
        # print("Current Level: " + str(current_level))
        # print("Max Probability: " + str(output_map_grid[current_level].max()))
        # print"========================================="
        ################################################

    elif data_type == const_GUI.data_type_2:
        # Liquid Water Content
        req_data = lwc_nc.variables['lwc'][:]

    elif data_type == const_GUI.data_type_3:
        # Relative Humidity
        rh_nc = Dataset(directory_pre + 'RH' + directory_post)
        req_data = rh_nc.variables['r'][:]
        vmin = 0
        vmax = 100

    elif data_type == const_GUI.data_type_4:
        # Relative Humidity Probability
        rhp_nc = Dataset(directory_pre + 'RHP' + directory_post)
        req_data = rhp_nc.variables['rhp'][:]
        vmin = 0
        vmax = 1

    elif data_type == const_GUI.data_type_5:
        # 'Temperature'
        t_nc = Dataset(directory_pre + 'T' + directory_post)
        req_data = t_nc.variables['t'][:] - ZERO_K

    elif data_type == const_GUI.data_type_6:
        # Temperature Probability
        tp_nc = Dataset(directory_pre + 'TP' + directory_post)
        req_data = tp_nc.variables['tp'][:]
        vmin = 0
        vmax = 1

    if cross_section == const_GUI.cross_section_1:
        # single geopotential level
        req_map_grid = np.squeeze(req_data[0, level, :, :])
        if not vmin:
            vmin = req_map_grid.min()
            vmax = req_map_grid.max()

        return req_map_grid, lats, lons, vmin, vmax
    else:
        # longitude slice or latitude slice
        output_map_grid = np.array([[], [], []])
        output_map_grid.resize(req_data.shape[1], req_data.shape[2], req_data.shape[3], refcheck=False)
        for current_level in range(level[0], level[-1] + 1):
            output_map_grid[level, :, :] = np.squeeze(req_data[0, level, :, :])

        if cross_section == const_GUI.cross_section_3:
            # longitude slice
            longitude_index = np.argmin(np.abs(cs_latlon - lons))
            print("Showing longitude slice for: " + str(lons[longitude_index]))
            lons = level.astype(float)

            req_map_grid = np.array(output_map_grid[:, :, longitude_index]).transpose()
            return req_map_grid, lats, lons, vmin, vmax
        else:
            # latitude slice
            latitude_index = np.argmin(np.abs(cs_latlon - lats))
            print("Showing latitude slice for: " + str(lats[latitude_index]))
            lats = level.astype(float)

            req_map_grid = np.flipud(np.array(output_map_grid[:, latitude_index, :]))
            return req_map_grid, lats, lons, vmin, vmax

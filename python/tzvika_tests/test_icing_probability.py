################################################
# This file produces icing probability maps in
# which the existance of a cloud is decided by
# an SLWC value higher than a threshold, and
# RH multiplied by air temperature is the probability.
################################################

import sys

from archive.structure import MDataset
from util.constants import *
from util.renderer import Renderer

# slwc_threshold = 0.2 # Anything above is considered a cloud
slwc_threshold = 0 # Anything above is considered a cloud

show_slice = 0  # 0 for flat single geopotential level, 1 for longitude slice and 2 for latitude slice
longitude_slice = 40  # degrees east
latitude_slice = 29  # degrees north

################################################
# open archive catalogue:
isobaric = MDataset("isobaric")

################################################
# get data grid coordinates:
time = isobaric.times[10]
# time = isobaric.times[27916]  # 09-Feb-2015
# levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
#              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
if show_slice == 0:
    level = [12]
else:
    level = np.array(range(19))

################################################
# get Temperature and RH grids:
t_var = isobaric['t']
rh_var = isobaric['rh']
lwc_var = isobaric['lwc'] # This is for testing the new nc files
rhp_var = isobaric['rhp'] # This is for testing the new nc files
tp_var = isobaric['tp'] # This is for testing the new nc files

################################################
# Calculate LWC: (NOT USED ANYMORE, because LWC
# is now pre-calculated
# lwc_var = calculate_lwc(time)
# Convert each cell above the threshold to 1 and
# each cell below the threshold to 0
# lwc_var = np.ceil(lwc_var - slwc_threshold)

################################################
# get working grid dimensions:
grid_width = t_var.shape[2]
grid_height = t_var.shape[3]

################################################
# calculate requested probability map
probability_grid = np.array([[], [], []])
probability_grid.resize(t_var.shape[1], t_var.shape[2], t_var.shape[3])

for current_level in range(level[0], level[-1]+1):

    if (t_var is None) or (lwc_var is None) or (rh_var is None):
        print "Failed to open sample grid"
        sys.exit()
    else:
        t_level = np.array(tp_var[time][current_level])
        rh_level = np.array(rhp_var[time][current_level])
        lwc_level = np.array(lwc_var[time][current_level])
        lwc_level = np.ceil(lwc_level - slwc_threshold)

    for width_idx in range(grid_width):
        for height_idx in range(grid_height):
            #RH_index = (np.abs(RH_probabilities[:, 0] - rh_level[width_idx, height_idx])).argmin()
            #RH_value = RH_probabilities[RH_index, 1]
            #temperature_index = (np.abs(temperature_probabilities[:, 0] - t_level[width_idx, height_idx] + ZERO_K)).argmin()
            #temperature_value = temperature_probabilities[temperature_index, 1]

            #probability_grid[current_level, width_idx, height_idx] = RH_value * temperature_value * lwc_level[width_idx, height_idx]
            #probability_grid[current_level, width_idx, height_idx] = RH_value * temperature_value
            #probability_grid[current_level, width_idx, height_idx] = t_level[width_idx, height_idx]
            #probability_grid[current_level, width_idx, height_idx] = rh_level[width_idx, height_idx]
            probability_grid[current_level, width_idx, height_idx] = rh_level[width_idx, height_idx] * t_level[width_idx, height_idx] * lwc_level[width_idx, height_idx]
    ################################################
    # Debug info:
    print("Current Level: " + str(current_level))
    print("Max Probability: " + str(probability_grid[current_level].max()))
    print"========================================="

################################################
# rendering either a single geopotential level
# or a vertical slice

if show_slice == 0:  # a single geopotential level
    renderer = Renderer(isobaric.lats, isobaric.lons, 0, 1)
    renderer.render(grid=probability_grid[level[0]], title="Icing Probability Map", units="Percents")
elif show_slice == 1:  # longitude slice
    longitude_index = np.argmin(np.abs(longitude_slice - isobaric.lons))
    print("Showing longitude slice for: " + str(isobaric.lons[longitude_index]))

    render_grid_probabilities = np.array(probability_grid[:, :, longitude_index])
    renderer = Renderer(isobaric.lats, level.astype(float), 0, 1)
    renderer.render(grid=render_grid_probabilities.transpose(), title="Converted Liquid Water Content", units="g/m3")
elif show_slice == 2:  # loatitude slice
    latitude_index = np.argmin(np.abs(latitude_slice - isobaric.lats))
    print("Showing latitude slice for: " + str(isobaric.lats[latitude_index]))

    render_grid_probabilities = np.array(probability_grid[:, latitude_index, :])
    renderer = Renderer(level.astype(float), isobaric.lons, 0, 1)
    renderer.render(grid=render_grid_probabilities, title="Converted Liquid Water Content", units="g/m3")
print "All done."

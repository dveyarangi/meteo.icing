################################################
# This file checks the validity of the conversion method
# offered at 12/3/2017 from SLWC to LWC, by comparing
# the conversion results with the relative humidity.
################################################

import numpy as np
import sys

from archive.structure import MDataset
from util.renderer import Renderer

show_slice = 3  # 0 for flat single geopotential level, 1 for longitude slice, 2 for latitude slice and 3 for sum of lwc
longitude_slice = 40  # degrees east
latitude_slice = 29  # degrees north

# ZERO_K = 273.15

################################################
# open archive catalogue:
isobaric = MDataset("isobaric")

################################################
# get data grid coordinates:
time = isobaric.times[10]
#time = isobaric.times[27916]  # 09-Feb-2015
# levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
#              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
if show_slice == 0:
    level = [15]
else:
    level = np.array(range(19))

################################################
# get SHUM grid, Temperature, CLWC and RH grids:
q_var = isobaric['q']
t_var = isobaric['t']
clwc_var = isobaric['clwc']
rh_var = isobaric['rh']

################################################
# calculate liquid water content in g/m3 values
lwc_grid = np.array([[], [], []])
lwc_grid.resize(q_var.shape[1], q_var.shape[2], q_var.shape[3])
lwc_sum_grid = np.array([[], []])
lwc_sum_grid.resize(q_var.shape[2], q_var.shape[3])
rh_grid = np.array([[], [], []])
rh_grid.resize(q_var.shape[1], q_var.shape[2], q_var.shape[3])

for current_level in range(level[0], level[-1]+1):
    pressure = isobaric.levels[current_level]*100  # Hectopascal to Pascal
    Rd = 287  # J K-1 Kg-1
    epsilon = 0.622  # Rd/Rv approximately in Earth's atmosphere

    if (q_var is None) or (t_var is None) or (clwc_var is None) or (rh_var is None):
        print "Failed to open sample grid"
        sys.exit()
    else:
        q_grid = np.array(q_var[time][current_level])
        t_grid = np.array(t_var[time][current_level])
        clwc_grid = np.array(clwc_var[time][current_level])
        current_rh_grid = np.array(rh_var[time][current_level])

    omega_grid = q_grid / (1 - q_grid)
    virtual_t_grid = t_grid * (epsilon + omega_grid) / (epsilon * (1 + omega_grid))
    air_density_grid = pressure/(Rd*virtual_t_grid)
    lwc_grid[current_level] = clwc_grid * air_density_grid * 1000  # *1000 o get to g/m3 instead of Kg/m3
    lwc_sum_grid = lwc_sum_grid + lwc_grid[current_level]
    rh_grid[current_level] = current_rh_grid
    print("Current Level: " + str(current_level))
    print("Max Air Density: " + str(air_density_grid[current_level].max()))
    print("Max CLWC: " + str(clwc_grid.max()))
    print("Max LWC: " + str(lwc_grid[current_level].max()))
    print"========================================="
print("Max LWC Sum: " + str(lwc_sum_grid.max()))
################################################
# get working grid dimensions:
grid_width = q_var.shape[2]
grid_height = q_var.shape[3]

################################################
# rendering either a single geopotential level
# or a vertical slice

if show_slice == 0:  # a single geopotential level
    renderer = Renderer(isobaric.lats, isobaric.lons, 0, 0.3)
    renderer.render(grid=lwc_grid[level[0]], title="Converted Liquid Water Content", units="g/m3")

    renderer = Renderer(isobaric.lats, isobaric.lons, 70, 100)
    renderer.render(grid=rh_grid[level[0]], title="Relative Humidity", units="%")
elif show_slice == 1:  # longitude slice
    longitude_index = np.argmin(np.abs(longitude_slice - isobaric.lons))
    print("Showing longitude slice for: " + str(isobaric.lons[longitude_index]))

    render_grid_lwc = np.array(lwc_grid[:, :, longitude_index])
    renderer = Renderer(isobaric.lats, level.astype(float), 0, 0.3)
    renderer.render(grid=render_grid_lwc.transpose(), title="Converted Liquid Water Content", units="g/m3")

    render_grid_rh = np.array(rh_grid[:, :, longitude_index])
    renderer = Renderer(isobaric.lats, level.astype(float), 70, 100)
    renderer.render(grid=render_grid_rh.transpose(), title="Relative Humidity", units="%")
elif show_slice == 2:  # loatitude slice
    latitude_index = np.argmin(np.abs(latitude_slice - isobaric.lats))
    print("Showing latitude slice for: " + str(isobaric.lats[latitude_index]))

    render_grid_lwc = np.array(lwc_grid[:, latitude_index, :])
    renderer = Renderer(level.astype(float), isobaric.lons, 0, 0.3)
    renderer.render(grid=render_grid_lwc, title="Converted Liquid Water Content", units="g/m3")

    render_grid_rh = np.array(rh_grid[:, latitude_index, :])
    renderer = Renderer(level.astype(float), isobaric.lons, 70, 100)
    renderer.render(grid=render_grid_rh, title="Relative Humidity", units="%")
elif show_slice == 3:  # a single geopotential level
    renderer = Renderer(isobaric.lats, isobaric.lons, 0, 1.5)
    renderer.render(grid=lwc_sum_grid, title="Converted Liquid Water Content", units="g/m3")

    renderer = Renderer(isobaric.lats, isobaric.lons, 70, 100)
    renderer.render(grid=rh_grid[level[15]], title="Relative Humidity", units="%")
print "All done."

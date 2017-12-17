import numpy as np
from netCDF4 import Dataset
import matplotlib.pyplot as plt
from wrf import getvar, interplevel, to_np, get_basemap, latlon_coords

from python.util.calculate_lwc_wrf import calculate_lwc_wrf

files_path = 'D:/DB/WRF'
req_year = '2016'
req_month = '12'
req_day = '14'
# req_day = '07'
dir_hour = '12'
req_hour = '10'

requested_level = 750

# levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
#              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
levels_list = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
requested_level_index = levels_list.index(requested_level)

# Open the NetCDF file
filename = files_path + '/' + req_year + req_month + req_day + dir_hour\
                + '/wrfout_d01_' + req_year + '-' + req_month + '-' + req_day + '_' + req_hour + '_00_00'
ncfile = Dataset(filename)

######################################################################################################
# pressure levels interpolation
######################################################################################################

LWC = calculate_lwc_wrf(ncfile)
LWC_at_req_level = LWC[requested_level_index]

# The calculations below are for creating the map
p = getvar(ncfile, "pressure")
z = getvar(ncfile, "z", units="dm")

# Interpolate geopotential height
ht_for_map_creation = interplevel(z, p, requested_level)

# Get the lat/lon coordinates
lats, lons = latlon_coords(ht_for_map_creation)

# Get the basemap object
bm = get_basemap(ht_for_map_creation)

# Create the figure
fig = plt.figure(figsize=(12,9))
ax = plt.axes()

# Convert the lat/lon coordinates to x/y coordinates in the projection space
x, y = bm(to_np(lons), to_np(lats))

# Add the geographic boundaries
bm.drawcoastlines(linewidth=0.25)
bm.drawstates(linewidth=0.25)
bm.drawcountries(linewidth=0.25)

plt.title( str(requested_level) + "hPa LWC")

# Add the requested data
lower_level = float(np.nanmin(LWC_at_req_level))
upper_level = float(np.nanmax(LWC_at_req_level))
levels = np.arange(lower_level, upper_level, (upper_level - lower_level)/20)
contours = bm.contourf(x, y, to_np(LWC_at_req_level), levels=levels)
# plt.clabel(contours, inline=1, fontsize=10, fmt="%i")

plt.colorbar(ax=ax, orientation="horizontal", pad=.05)


plt.show()

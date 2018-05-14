import numpy as np
from netCDF4 import Dataset
import matplotlib.pyplot as plt
from wrf import getvar, interplevel, to_np, get_basemap, latlon_coords
import matplotlib.image as mpimg


from python.util.calculate_lwc_wrf import calculate_lwc_wrf
from python.util.calculate_icing_wrf import calculate_icing_wrf

files_path = 'D:/WRFOUT'
req_year = '2016'
req_month = '12'
req_day = '14'
# req_day = '07'
dir_hour = '12'
req_hour = '10'

requested_level = 850

meteosat_dust_files_path = 'D:/MeteoSat/DUST-IR_8.7_10.8_12.0/'

# slwc_threshold = 0.2 # Anything above is considered a cloud
slwc_threshold = 0 # Anything above is considered a cloud

show_slice = 0  # 0 for flat single geopotential level, 1 for longitude slice and 2 for latitude slice
longitude_slice = 40  # degrees east
latitude_slice = 29  # degrees north

# levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
#              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
levels_list = [100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000]
requested_level_index = levels_list.index(requested_level)

if show_slice == 0:
    level = requested_level_index
else:
    level = np.array(range(19))


# Open the NetCDF file
# filename = files_path + '/' + req_year + req_month + req_day + dir_hour\
#                 + '/wrfout_d01_' + req_year + '-' + req_month + '-' + req_day + '_' + req_hour + '_00_00'
filename = files_path + '/' + req_year + '/' + req_month + '/' + req_year + req_month + req_day + dir_hour\
                + '/wrfout_d01_' + req_year + '-' + req_month + '-' + req_day + '_' + req_hour + '_00_00'
ncfile = Dataset(filename)

######################################################################################################
# pressure levels interpolation
######################################################################################################

LWC = calculate_lwc_wrf(ncfile)
icing_prob = calculate_icing_wrf(ncfile, LWC, slwc_threshold)
icing_prob_at_req_level = icing_prob[requested_level_index]

# The calculations below are for creating the map
p = getvar(ncfile, "pressure")
z = getvar(ncfile, "z", units="dm")

fig = plt.figure(figsize=(10, 7))
ax_wrf = fig.add_subplot(1, 2, 1)

# Interpolate geopotential height
ht_for_map_creation = interplevel(z, p, requested_level)

# Get the lat/lon coordinates
lats, lons = latlon_coords(ht_for_map_creation)

# Get the basemap object
bm = get_basemap(ht_for_map_creation)

# Create the figure
# fig = plt.figure(figsize=(12,9))
#ax = plt.axes()

# Convert the lat/lon coordinates to x/y coordinates in the projection space
x, y = bm(to_np(lons), to_np(lats))

# Add the geographic boundaries
bm.drawcoastlines(linewidth=0.25, ax=ax_wrf)
bm.drawstates(linewidth=0.25, ax=ax_wrf)
bm.drawcountries(linewidth=0.25, ax=ax_wrf)

plt.title( str(requested_level) + "hPa Icing Probabilities")

# Add the requested data
#lower_level = float(np.nanmin(icing_prob_at_req_level))
#upper_level = float(np.nanmax(icing_prob_at_req_level))
levels = np.arange(0, 1.05, 0.05)
contours = bm.contourf(x, y, to_np(icing_prob_at_req_level), levels=levels, ax=ax_wrf)
# plt.clabel(contours, inline=1, fontsize=10, fmt="%i")

# plt.colorbar(orientation="horizontal", pad=.05)


#plt.show()

# Show the dust METEOSAT image
fig.add_subplot(1, 2, 2)

image_filename = meteosat_dust_files_path + req_year + req_month + req_day + '_' + req_hour + '00.gif'
img=mpimg.imread(image_filename)
img = img[270:800, 430:700, :]
imgplot = plt.imshow(img)  # , interpolation='nearest', aspect='auto', extent=[0, 100, 0, 100*(img.shape[0]/img.shape[1])])
plt.show()

# ################################################
# # rendering either a single geopotential level
# # or a vertical slice
#
# if show_slice == 0:  # a single geopotential level
#     renderer = Renderer(isobaric.lats, isobaric.lons, 0, 1)
#     renderer.render(grid=probability_grid[level[0]], title="Icing Probability Map", units="Percents")
# elif show_slice == 1:  # longitude slice
#     longitude_index = np.argmin(np.abs(longitude_slice - isobaric.lons))
#     print("Showing longitude slice for: " + str(isobaric.lons[longitude_index]))
#
#     render_grid_probabilities = np.array(probability_grid[:, :, longitude_index])
#     renderer = Renderer(isobaric.lats, level.astype(float), 0, 1)
#     renderer.render(grid=render_grid_probabilities.transpose(), title="Converted Liquid Water Content", units="g/m3")
# elif show_slice == 2:  # loatitude slice
#     latitude_index = np.argmin(np.abs(latitude_slice - isobaric.lats))
#     print("Showing latitude slice for: " + str(isobaric.lats[latitude_index]))
#
#     render_grid_probabilities = np.array(probability_grid[:, latitude_index, :])
#     renderer = Renderer(level.astype(float), isobaric.lons, 0, 1)
#     renderer.render(grid=render_grid_probabilities, title="Converted Liquid Water Content", units="g/m3")
# print "All done."
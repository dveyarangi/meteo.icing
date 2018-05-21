import numpy as np
from netCDF4 import Dataset
import matplotlib.pyplot as plt
from mpl_toolkits.basemap import Basemap
from wrf import getvar, interplevel, to_np, get_basemap, latlon_coords
import matplotlib.image as mpimg


from python.util.calculate_lwc_wrf import calculate_lwc_wrf
from python.util.calculate_icing_wrf import calculate_icing_wrf
from display_maps_GUI.calculate_icing_probability import calculate_icing_probability

files_path = 'D:/WRFOUT'
req_year = '2016'
req_month = '12'
req_day = '14'
# req_day = '07'
dir_hour = '12'
req_hour = '12'

requested_level = 850

meteosat_dust_files_path = 'D:/MeteoSat/DUST-IR_8.7_10.8_12.0/'

# slwc_threshold = 0.2 # Anything above is considered a cloud
slwc_threshold = 0 # Anything above is considered a cloud

show_slice = 0  # 0 for flat single geopotential level, 1 for longitude slice and 2 for latitude slice
# longitude_slice = 40  # degrees east
# latitude_slice = 29  # degrees north

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
plt.suptitle(req_year + '-' + req_month + '-' + req_day + '-' + req_hour + ' at level ' + str(requested_level) + 'hPa', fontsize=16)

#############################################
# First part is the WRF icing probabilities #
#############################################

ax_wrf = fig.add_subplot(1, 3, 1)

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

plt.title('WRF ' + str(requested_level) + "hPa Icing Probabilities ")

# Add the requested data
#lower_level = float(np.nanmin(icing_prob_at_req_level))
#upper_level = float(np.nanmax(icing_prob_at_req_level))
levels = np.arange(0, 1.05, 0.05)
contours = bm.contourf(x, y, to_np(icing_prob_at_req_level), levels=levels, ax=ax_wrf)
# plt.clabel(contours, inline=1, fontsize=10, fmt="%i")

# plt.colorbar(orientation="horizontal", pad=.05)


#plt.show()

##########################################
# Second part is the dust METEOSAT image #
##########################################

fig.add_subplot(1, 3, 2)
plt.title('Meteosat Dust')

image_filename = meteosat_dust_files_path + req_year + req_month + req_day + '_' + req_hour + '00.gif'
img=mpimg.imread(image_filename)
img = img[270:800, 430:700, :]
imgplot = plt.imshow(img)  # , interpolation='nearest', aspect='auto', extent=[0, 100, 0, 100*(img.shape[0]/img.shape[1])])

#############################################
# Third part is the ERA icing probabilities #
#############################################

# The first part is completely done by matplotlib, and then transferred to Tkinter
req_map_grid, lats_2, lons_2, vmin, vmax = calculate_icing_probability('Icing Probability', 'GPH level',
                                                                   str(requested_level), float(40),
                                                                   float(0), 'E:/DB/Era_Interim',
                                                                   req_year, req_month, req_day,
                                                                   req_hour)
map_axis = fig.add_subplot(1, 3, 3)
plt.title('ERA ' + str(requested_level) + "hPa Icing Probabilities ")

my_map = Basemap(projection='mill',
                 lat_0=lats.min(),
                 lon_0=lons.min(),
                 lat_1=lats.max(),
                 lon_1=lons.max(),
                 llcrnrlat=lats.min(), urcrnrlat=lats.max(),
                 llcrnrlon=lons.min(), urcrnrlon=lons.max(),
                 ax=map_axis
                 )
lon, lat = np.meshgrid(lons_2, lats_2)
xi, yi = my_map(lon, lat)
cs = my_map.pcolor(xi, yi, np.squeeze(req_map_grid), vmin=vmin, vmax=vmax)

# Add Grid Lines and Geography
my_map.drawparallels(np.arange(lats.min(), lats.max(), 10.), labels=[1, 0, 0, 0], fontsize=8)
my_map.drawmeridians(np.arange(lons.min(), lons.max(), 10.), labels=[0, 0, 0, 1], fontsize=8)
# Add Coastlines, States, and Country Boundaries
# my_map.fillcontinents()
my_map.drawcoastlines()
my_map.drawstates()
my_map.drawcountries()

# Add Colorbar
# cbar = map_figure.colorbar(cs, orientation='horizontal')
# cbar.set_label('Percents')

plt.show()

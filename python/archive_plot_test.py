
from archive.structure import MDataset

import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.basemap import Basemap

################################################
# simple read example:


isobaric = MDataset("isobaric")


time = isobaric.times[0]
level = 6

t_var = isobaric['t']

t_grid = t_var[time][level]

print("Grid size: ", t_grid.shape)
print ("Level: ", isobaric.levels[level])
print("Units: ", t_var.units)

####################################
# iteration example

print ("Calculating test grid...")
# get sum grid dimensions:
grid_width  = t_grid.shape[0]
grid_height = t_grid.shape[1]

# create accumulation array:
accu = np.ndarray(shape=(grid_width, grid_height),dtype=float)

# iterate over the archive:
for time in isobaric.times:
    
    if time.hour is not 00: continue

    accu += t_var[time][level] 


accu /= len(isobaric.times)

#####################################
# rendering
print ("Rendering...")

lons = isobaric.lons
lats = isobaric.lats

# Get some parameters for projection
lon_0 = lons.mean()
lat_0 = lats.mean()
'''
m = Basemap(width=50000000,height=35000000,
            resolution='l',projection='mill',\
            lat_ts=-90,lat_0=lat_0,lon_0=lon_0)
'''            

print("lons: ",lons.min(),":",lons.max())
print("lats: ",lats.min(),":",lats.max())

m = Basemap(projection='mill',lat_0=lat_0,lon_0=lon_0)


# Because our lon and lat variables are 1D, 
# use meshgrid to create 2D arrays 
# Not necessary if coordinates are already in 2D arrays.
lon, lat = np.meshgrid(lons, lats)
xi, yi = m(lon, lat)


# Plot Data

cs = m.pcolor( xi, yi, np.squeeze(accu) )

# Add Grid Lines
m.drawparallels(np.arange(-80.,   81., 10.), labels=[1,0,0,0], fontsize=10)
m.drawmeridians(np.arange(-180., 181., 10.), labels=[0,0,0,1], fontsize=10)

# Add Coastlines, States, and Country Boundaries
#m.fillcontinents()
m.drawcoastlines()
m.drawstates()
m.drawcountries()

# Add Colorbar
cbar = m.colorbar(cs, location='bottom', pad="10%")
cbar.set_label(t_var.units)

# Add Title
plt.title('ERA-interim test')

plt.show()
    
    
    
    
    
    
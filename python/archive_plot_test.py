
from archive.structure import MDataset
from util.renderer import Renderer

import numpy as np


################################################
# simple read example:


isobaric = MDataset("isobaric")

renderer = Renderer(isobaric.lats, isobaric.lons)

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
accu = np.zeros(shape=(grid_width, grid_height),dtype=float)
count = np.zeros(shape=(grid_width, grid_height),dtype=float)

totalGrids = 0
# iterate over the archive:
for time in isobaric.times:
    
    if time.hour is not 00: continue

    t_slice = t_var[time];
 #   print (t_slice)
    if t_slice is not None:
        grid = t_slice[level]
        accu += grid
        for (x,y),value in np.ndenumerate(grid):
            if 273.15 > value > 273.15-20:
                count[x][y] += 1
        totalGrids += 1

accu /= len(isobaric.times)

count /= totalGrids

file = open('temperature.dat', 'w')
for x in xrange(count.shape[0]):
    for y in xrange(count.shape[1]):
       # file.write()
        print str(count[x][y]) + " "
    print "\n"

file.close()
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

cs = m.pcolor( xi, yi, np.squeeze(count) )

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
    
    
    
    
    
    
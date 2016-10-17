
from netCDF4 import Dataset
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.basemap import Basemap

archive_folder_root = 'E:/Development/workspaces/meteo/ecmwf-api-client/download/era-interim'
nc_file = archive_folder_root + '/2016-05/30/' + '2016-05-30_00Z_T_isobaric.nc'

fh = Dataset(nc_file, mode='r')


time = 0
level = 0

lons = fh.variables['longitude'][:]
lats = fh.variables['latitude'][:]
print(lons.shape)
print(lats.shape)
temperature = fh.variables['t'][time][level][:]
print(temperature.shape)
print(fh.variables['t'].shape)

temperature_units = fh.variables['t'].units

# Get some parameters for the Stereographic Projection
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

flat_temperature = np.squeeze(temperature);

cs = m.pcolor( xi, yi, np.squeeze(temperature) )

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
cbar.set_label(temperature_units)

# Add Title
plt.title('ERA-interim test')

plt.show()
    
    
    
    
    
    
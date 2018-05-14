
from netCDF4 import Dataset
import numpy as np
from util.renderer import Renderer

archive_folder_root = 'E:/Development/workspaces/meteo/ecmwf-api-client/download/era-interim'
nc_file = 'E:\meteo\dropbox\Dropbox\Icing\MS\model-ecmwf-000-20161201_000000.nc'

fh = Dataset(nc_file, mode='r')


time = 0
level = 7

lons = fh.variables['longitude'][:]
lats = fh.variables['latitude'][:]

renderer = Renderer( lats, lons )

print(lons.shape)
print(lats.shape)
temperature = fh.variables['t'][time][level][:]
print(temperature.shape)
print(fh.variables['t'].shape)

temperature_units = fh.variables['t'].units

# Get some parameters for the Stereographic Projection

renderer.render( temperature, 'ERA-interim test', temperature_units)

    
    
    
    
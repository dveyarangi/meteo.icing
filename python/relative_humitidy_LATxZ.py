import numpy as np
import sys
import datetime

import matplotlib.pyplot as plt

from archive.structure import MDataset
from util.renderer import Renderer
import util.renderer as R

################################################
# open archive catalogue:
isobaric = MDataset("isobaric")

# define required grid parameters:

parameter = 'rh';

time = datetime.datetime(2011, 8, 16)

lat = 32
lon = 35

# get 3d grid for specified time and parameter:

var = isobaric[parameter]
grid3d = var[time]

################################################
# select vertical slice along "lon_index" line

lon_index = isobaric.closestLonIndex(lon)
grid2d = grid3d[:,:,lon_index]

#lat_index = isobaric.closestLatIndex(lat)
#grid2d = grid3d[:,lat_index,:] # cuts the grid along specified dimension at specified index

#grid2d = np.swapaxes(grid2d, 0, 1)

################################################
# print debug info
if var is not None: 
    print("Grid properties:")
    grid_width  = grid2d.shape[0]
    grid_height = grid2d.shape[1]
    print(" * Grid size: " + str(grid_width) + "x" + str(grid_height))
    print(" * Lat: %d" % lat)
    print(" * Units: " + str(var.units))
else:
    print "Failed to open sample grid"
    sys.exit()


#####################################
# rendering

R.draw_heatmap( grid2d, 
                xaxis=isobaric.lons, 
                yaxis=isobaric.levels, 
                title="%s vertical slice at lat=%s , %s" % (parameter, lat, time), 
                vmin=0, 
                vmax=100
            )

print "All done."

                   
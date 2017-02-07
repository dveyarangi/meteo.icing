import numpy as np
import sys
import datetime

import matplotlib.pyplot as plt

from archive.structure import MDataset
from util.renderer import Renderer


################################################
# open archive catalogue:
isobaric = MDataset("isobaric")


################################################
# get data grid coordinates:
parameter = 'rh';
time = datetime.datetime(2011, 1, 1)

lon = 35
lon_index = isobaric.closestLonIndex(lon)

################################################
# get physical parameter grid:
var = isobaric[parameter]

grid3d = var[time]

################################################
# selecting vertical slice along "lon_index" line
grid2d = grid3d[:,:,lon_index]
# grid2d = grid3d[:,lat_index,:]

################################################
# verify data is read
if var is not None: 

    grid_width  = grid2d.shape[0]
    grid_height = grid2d.shape[1]
    print("Grid size: " + str(grid_width) + "x" + str(grid_height))
    print("Lon: %d" % lon)
    print("Units: " + str(var.units))
else:
    print "Failed to open sample grid"
    sys.exit()

totalGrids = 0

################################################
# calculating physical value distribution histogram
   
# iterate over the archive:

# time range:

title = "Relative humidity vertical slice at lon=%s , %s" % (lon, time)

#####################################
# rendering

# naming levels (y) axis ticks:
yValues = []
yLabels = []
for (idx, level) in enumerate(isobaric.levels) :
    yValues.append(idx)
    yLabels.append(str(isobaric.levels[idx]))
plt.yticks(yValues, yLabels)
                   

# labeling lats (x) axis ticks:
xValues = []
xLabels = []
for (idx, lat) in enumerate(isobaric.lats) :
    if idx % 3 == 0:
        xValues.append(idx)
        xLabels.append(lat)
plt.xticks(xValues, xLabels)

# drawing things:
plt.imshow(grid2d, cmap='hot', interpolation='nearest', vmin=0, vmax=100)
plt.title( title )
plt.colorbar(orientation="horizontal") 
plt.show()


print "All done."
    
    
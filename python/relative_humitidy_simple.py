import numpy as np
import sys
import datetime

from archive.structure import MDataset
from util.renderer import Renderer


################################################
# open archive catalogue:
isobaric = MDataset("isobaric")


################################################
# get data grid coordinates:
parameter = 'rh';
time = isobaric.times[0]
level = 15 # 850mb
#level = 8 # 500mb
# time range:
time = datetime.datetime(2011, 1, 1)

################################################
# get physical parameter grid:
var = isobaric[parameter]


################################################
# get working grid dimensions:

################################################
# verify data is read
if var is not None: 
    grid2d = var[time][level]

    grid_width  = grid2d.shape[0]
    grid_height = grid2d.shape[1]
    level_name = str(isobaric.levels[level])
    print("Grid size: " + str(grid_width) + "x" + str(grid_height))
    print("Level: " + level_name)
    print("Units: " + str(var.units))
    title = "Relative humidity at %smb (00Z), %s" % (level_name, time)

else:
    print "Failed to open sample grid"
    sys.exit()

totalGrids = 0

################################################
# calculating physical value distribution histogram
   
# iterate over the archive:


# getting 3D slice:
slice3D = var[time];

# getting 2D slice:
slice2D = slice3D[level]

#####################################
# rendering

renderer = Renderer( isobaric.lats, isobaric.lons, 0, 100 )

renderer.render( grid=slice2D, title=title, units="P" )

print "All done."
    
    
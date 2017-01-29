
from archive.structure import MDataset

import numpy as np
from util.renderer import Renderer

################################################
# simple read example:

isobaric = MDataset("isobaric")

renderer = Renderer( isobaric.lats, isobaric.lons )

time = isobaric.times[0]
level = 15 # 850mb
#level = 8 # 500mb

if t_var is not None: 
    t_grid = t_var[time][level]

    ################################################
    # debug
    print("Grid size: " + str(t_grid.shape[0]) + "x" + str(t_grid.shape[1]))
    print("Level: " + str(isobaric.levels[level]))
    print("Units: " + str(t_var.units))
else:
    print "Failed to open sample grid"
    sys.exit()

####################################
# iteration example
# get sum grid dimensions:
grid_width  = t_grid.shape[0]
grid_height = t_grid.shape[1]

min = -24
max = -0

histogram_size = max - min

# create accumulation array:
accu = np.zeros(shape=(grid_width, grid_height),dtype=float)
count = np.zeros(shape=(grid_width, grid_height),dtype=float)

ZERO_K = 273.15

totalGrids = 0
title = "Probability of T in [%d, %d] at %smb (00Z)"% (min, max, isobaric.levels[level])

print ("Calculating %s" % title)
# iterate over the archive:
for time in isobaric.times:
    
    if time.hour is not 00: continue

    t_slice = t_var[time];
 #   print (t_slice)
    if t_slice is not None:
        grid = t_slice[level]
        accu += grid
        for (x,y),value in np.ndenumerate(grid):
            if ZERO_K+min <= value <= ZERO_K+max:
                count[x][y] += 1
        totalGrids += 1
        print time

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


renderer.render( count, title, "P")
    
#savefig('foo.png', bbox_inches='tight')
    
    
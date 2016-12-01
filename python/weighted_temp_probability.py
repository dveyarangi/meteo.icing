
from archive.structure import MDataset

import numpy as np
from util.renderer import Renderer

################################################
# simple read example:

with open('t.prob') as f:
    icing_prob = [[float(x) for x in line.split()] for line in f]
weights_histogram = list(enumerate(icing_prob))
#for idx, (t, prob) in weights_histogram:
#    print idx, t, prob
    
isobaric = MDataset("isobaric")

renderer = Renderer( isobaric.lats, isobaric.lons )



time = isobaric.times[0]
level = 15 # 850mb
#level = 8 # 500mb

t_var = isobaric['t']

t_grid = t_var[time][level]

print("Grid size: ", t_grid.shape)
print ("Level: ", isobaric.levels[level])
print("Units: ", t_var.units)

####################################
# iteration example
# get sum grid dimensions:
grid_width  = t_grid.shape[0]
grid_height = t_grid.shape[1]


# create accumulation array:
count = np.zeros(shape=(grid_width, grid_height, len(icing_prob)),dtype=float)

ZERO_K = 273.15

totalGrids = 0
title = "Weighted probability at %smb (00Z)"% (isobaric.levels[level])


            
    
print ("Calculating %s..." % title)
# iterate over the archive:
for time in isobaric.times:
    
    if time.hour is not 00: continue

    t_slice = t_var[time];
 #   print (t_slice)
    if t_slice is not None:
        grid = t_slice[level]
        
        for (x,y),value in np.ndenumerate(grid):
            for idx, (temp, prob) in weights_histogram:
                if ZERO_K+temp-0.5 <= value <= ZERO_K+temp+0.5:
                    count[x][y][idx] += 1
        totalGrids += 1
        print time
count /= totalGrids

weighted_count = np.zeros(shape=(grid_width, grid_height),dtype=float)

for x in xrange(count.shape[0]):
    for y in xrange(count.shape[1]):
        for idx, (t, prob) in weights_histogram:
            weighted_count[x][y] += count[x][y][idx] * prob

#####################################
# rendering


renderer.render( weighted_count, title, "P")
    
#savefig('foo.png', bbox_inches='tight')
    
    
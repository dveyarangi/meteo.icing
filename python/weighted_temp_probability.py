
from archive.structure import MDataset

import numpy as np
from util.renderer import Renderer


ZERO_K = 273.15


################################################
# read probabilities histogram from file:

with open('t.prob') as f:
    icing_prob = [[float(x) for x in line.split()] for line in f]
weights_histogram = list(enumerate(icing_prob))
    
    
################################################
# open archive catalogue:
isobaric = MDataset("isobaric")


################################################
# get data grid coordinates:
time = isobaric.times[0]
level = 15 # 850mb
#level = 8 # 500mb

################################################
# get physical parameter grid:
t_var = isobaric['t']

t_grid = t_var[time][level]


################################################
# debug
print("Grid size: ", t_grid.shape)
print ("Level: ", isobaric.levels[level])
print("Units: ", t_var.units)

totalGrids = 0
title = "Weighted probability at %smb (00Z)"% (isobaric.levels[level])

################################################
# get working grid dimensions:
grid_width  = t_grid.shape[0]
grid_height = t_grid.shape[1]

################################################
# create accumulation array for probabilities calculation:
# width * height * histogram size  
count = np.zeros(shape=(grid_width, grid_height, len(icing_prob)),dtype=float)
            
################################################
# calculating physical value distribution histogram
   
print ("Calculating %s..." % title)
# iterate over the archive:
for time in isobaric.times:
    
    # time filter:
    if time.hour is not 00: continue

    # getting 3D slice:
    t_slice = t_var[time];
    
    if t_slice is not None:
        # getting 2D slice:
        grid = t_slice[level]
        
        # for every grid point, increase physical value bin
        for (x,y),value in np.ndenumerate(grid):
            for idx, (temp, prob) in weights_histogram:
                if ZERO_K+temp-0.5 <= value <= ZERO_K+temp+0.5:
                    count[x][y][idx] += 1
                    
        totalGrids += 1
        
        print time
        
count /= totalGrids

################################################
# prepare probabilities grid:
weighted_count = np.zeros(shape=(grid_width, grid_height),dtype=float)

# calculate probabilities:
for x in xrange(count.shape[0]):
    for y in xrange(count.shape[1]):
        for idx, (t, prob) in weights_histogram:
            weighted_count[x][y] += count[x][y][idx] * prob

#####################################
# rendering

renderer = Renderer( isobaric.lats, isobaric.lons )

renderer.render( weighted_count, title, "P")
    
#savefig('foo.png', bbox_inches='tight')
    
    
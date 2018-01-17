
from archive.structure import MDataset

import numpy as np
from util.renderer import Renderer
import calendar

################################################
# simple read example:

isobaric = MDataset("isobaric")

renderer = Renderer( isobaric.lats, isobaric.lons )

time = isobaric.times[0]
level = 12 # 850mb
#level = 8 # 500mb

parameters = ["u", "v"]

basehours = [0, 12]

ZERO_K = 273.15


month_map = {}

lat = 32.4340 
lon = 34.9197

y = isobaric.closestLonIndex( lon )
x = isobaric.closestLatIndex( lat )
print "%f,%f" % (isobaric.lons[y], isobaric.lats[x])

print "%f,%d" % (x, y)

#calendar = Calendar()

# iterate over the archive:
for parameter in parameters:
    
    var = isobaric[parameter]
    
    for basehour in basehours:
        title = "Average of %s at %smb (%dZ)"% (parameter, isobaric.levels[level], basehour)

        print ("%s" % title)

        for month in range(1, 13): 
            
            month_accum = 0
            month_count = 0
            for time in isobaric.times:
                
                if time.hour not in basehours: continue
                if time.month is not month: continue
            
                slice = var[time];
                if slice is None: continue
                
                grid = slice[level]
 #               print grid.shape
 #               print("Grid size: " + str(grid.shape[0]) + "x" + str(grid.shape[1]))
              
                value = grid[x,y]
                
                month_accum += value
                month_count += 1
                
         
                #print time
            
            month_map[month] = month_accum / month_count
            print "%s    %f" % (calendar.month_abbr[month], month_map[month])
    
    
#file = open('average.dat', 'w')
#for x in months:
#    for y in xrange(count.shape[1]):
#       # file.write()
#        print str(count[x][y]) + " "
#    print "\n"
#
#file.close()
#####################################
# rendering


#renderer.render( count, title, "P")
    
#savefig('foo.png', bbox_inches='tight')
    
    
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.basemap import Basemap

class Renderer:
    
    def __init__(self, lats, lons, vmin = 0, vmax = 1):   
        
        self.lats = lats
        self.lons = lons
        
        self.vmin = vmin
        self.vmax = vmax

        # Get some parameters for projection
        lon_0 = lons.mean()
        lat_0 = lats.mean()
        '''
        m = Basemap(width=50000000,height=35000000,
                    resolution='l',projection='mill',\
                    lat_ts=-90,lat_0=lat_0,lon_0=lon_0)
        '''            
        
        print("lons: " + str(lons.min()) + ":" + str(lons.max()))
        print("lats: " + str(lats.min()) + ":" + str(lats.max()))
        
        self.map = Basemap(projection='mill',
                           lat_0=lats.min(),
                           lon_0=lons.min(), 
                           lat_1=lats.max(),
                           lon_1=lons.max(),
                           llcrnrlat=lats.min(), urcrnrlat=lats.max(),
                           llcrnrlon=lons.min(), urcrnrlon=lons.max()
                           )
         # Because our lon and lat variables are 1D, 
        # use meshgrid to create 2D arrays 
        # Not necessary if coordinates are already in 2D arrays.
        lon, lat = np.meshgrid(lons, lats)
        self.xi, self.yi = self.map(lon, lat)
    
       
    
    def render(self, grid, title, units):
        print ("Rendering...")

        # Plot Data
        
        m = self.map
        
        cs = m.pcolor( self.xi, self.yi, np.squeeze(grid), vmin=self.vmin, vmax=self.vmax )
        
        # Add Grid Lines
        m.drawparallels(np.arange(self.lats.min(), self.lats.max(), 10.), labels=[1,0,0,0], fontsize=10)
        m.drawmeridians(np.arange(self.lons.min(), self.lons.max(), 10.), labels=[0,0,0,1], fontsize=10)
        
        # Add Coastlines, States, and Country Boundaries
        #m.fillcontinents()
        m.drawcoastlines()
        m.drawstates()
        m.drawcountries()
        
        # Add Colorbar
        cbar = m.colorbar(cs, location='bottom', pad="10%")
        cbar.set_label(units)
        
        # Add Title
        plt.title( title )
        
        plt.show()
        
def create_axis_labels(array, step=1):
    values = []
    labels = []
    for (idx, value) in enumerate(array) :
        if idx % step == 0:
            values.append(idx)
            labels.append(str(value))
    return (values, labels)

def draw_heatmap( grid2d, xaxis, yaxis, title="", vmin=0, vmax=1 ):
    # name levels (y) axis ticks:
    (yValues, yLabels) = create_axis_labels( yaxis, 2 )
    plt.yticks(yValues, yLabels)
    # name lats (x) axis ticks:
    (xValues, xLabels) = create_axis_labels( xaxis, 5 )
    plt.xticks(xValues, xLabels)
    
    # prepare heatmap:
    plt.imshow(grid2d, cmap='hot', interpolation='nearest', vmin=vmin, vmax=vmax)
    
    # set title
    plt.title( title )
    
    # adding color legend:
    plt.colorbar(orientation="horizontal") 
    
    # show the image:
    plt.show()
    
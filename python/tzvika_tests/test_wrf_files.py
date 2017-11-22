import numpy as np
from netCDF4 import Dataset
import matplotlib.pyplot as plt
from matplotlib.cm import get_cmap
from mpl_toolkits.basemap import Basemap
from wrf import getvar, interplevel, smooth2d, to_np, get_basemap, latlon_coords

show_slp = 1

files_path = 'D:/DB/WRF'
req_year = '2016'
req_month = '12'
req_day = '07'
dir_hour = '12'
req_hour = '10'
# levels(hPa): 0=100 1=150 2=200 3=250 4=300 5=350 6=400 7=450 8=500 9=550
#              10=600 11=650 12=700 13=750 14=800 15=850 16=900 17=950 18=1000
#levels = range(100,1001, 50)
#current_level = 15

# Open the NetCDF file
filename = files_path + '/' + req_year + req_month + req_day + dir_hour\
                + '/wrfout_d01_' + req_year + '-' + req_month + '-' + req_day + '_' + req_hour + '_00_00'
ncfile = Dataset(filename)

if show_slp:
    ######################################################################################################
    # SLP
    ######################################################################################################
    # Get the sea level pressure
    slp = getvar(ncfile, "slp")

    # Smooth the sea level pressure since it tends to be noisy near the mountains
    smooth_slp = smooth2d(slp, 3)

    # Get the latitude and longitude points
    lats, lons = latlon_coords(slp)

    # Get the basemap object
    bm = get_basemap(slp)

    # Create a figure
    fig = plt.figure(figsize=(12,9))

    # Add geographic outlines
    bm.drawcoastlines(linewidth=0.25)
    bm.drawstates(linewidth=0.25)
    bm.drawcountries(linewidth=0.25)

    # Convert the lats and lons to x and y.  Make sure you convert the lats and lons to
    # numpy arrays via to_np, or basemap crashes with an undefined RuntimeError.
    x, y = bm(to_np(lons), to_np(lats))

    # Draw the contours and filled contours
    bm.contour(x, y, to_np(smooth_slp), 10, colors="black")
    bm.contourf(x, y, to_np(smooth_slp), 10, cmap=get_cmap("jet"))

    # Add a color bar
    plt.colorbar(shrink=.62)

    plt.title("Sea Level Pressure (hPa)")

    plt.show()
else:
    ######################################################################################################
    # pressure levels interpolation
    ######################################################################################################

    # Extract the pressure, geopotential height, and wind variables
    p = getvar(ncfile, "pressure")
    z = getvar(ncfile, "z", units="dm")

    # Interpolate geopotential height
    ht_500 = interplevel(z, p, 500)

    # Get the lat/lon coordinates
    lats, lons = latlon_coords(ht_500)

    # Get the basemap object
    bm = get_basemap(ht_500)

    # Create the figure
    fig = plt.figure(figsize=(12,9))
    ax = plt.axes()

    # Convert the lat/lon coordinates to x/y coordinates in the projection space
    x, y = bm(to_np(lons), to_np(lats))

    # Add the 500 hPa geopotential height contours
    levels = np.arange(500., 580., 6.)
    contours = bm.contourf(x, y, to_np(ht_500), levels=levels)
    plt.clabel(contours, inline=1, fontsize=10, fmt="%i")

    # Add the wind speed contours
    levels = [25, 30, 35, 40, 50, 60, 70, 80, 90, 100, 110, 120]

    plt.colorbar(ax=ax, orientation="horizontal", pad=.05)

    # Add the geographic boundaries
    bm.drawcoastlines(linewidth=0.25)
    bm.drawstates(linewidth=0.25)
    bm.drawcountries(linewidth=0.25)

    plt.title("500 MB Height (dm)")

    plt.show()

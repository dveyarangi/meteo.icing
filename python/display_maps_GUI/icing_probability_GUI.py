from sys import version_info

import matplotlib
import numpy as np
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg, NavigationToolbar2TkAgg
from matplotlib.figure import Figure
from mpl_toolkits.basemap import Basemap

from display_maps_GUI.calculate_icing_probability import calculate_icing_probability
import display_maps_GUI.constants_display_maps_GUI as const_GUI
matplotlib.use('TkAgg')

if version_info[0] < 3:
    import Tkinter as tk
else:
    import tkinter as tk


class Icing_Probability_GUI:
    def __init__(self, master):
        self.master = master
        self.master.title(const_GUI.title)
        self.master.rowconfigure(1, weight=1)

        # Define the GUI frames
        self.frame_data_type = tk.Frame(self.master)
        self.frame_cross_section = tk.Frame(self.master)
        self.frame_cs_attributes = tk.Frame(self.master)
        self.frame_map = tk.Frame(self.master)
        self.frame_nav_toolbar = tk.Frame(self.master)

        # Define the GUI frame's layout
        self.frame_data_type.grid(row=0, column=0, sticky=tk.N, padx=5, pady=5)
        self.frame_cross_section.grid(row=0, column=1, sticky=tk.N, padx=5, pady=5)
        self.frame_cs_attributes.grid(row=0, column=2, sticky=tk.N, padx=5, pady=5)
        self.frame_map.grid(row=1, columnspan=3)
        self.frame_map.rowconfigure(0, weight=1)
        self.frame_nav_toolbar.grid(row=2, columnspan=3)

        # Define the data type widgets (1=Icing Prob. 2=LWC, 3=RH, 4=RHP, 5=T, 6=TP)
        self.data_type_label = tk.Label(self.frame_data_type, text=const_GUI.data_type_label)
        self.selected_data_type = tk.StringVar()
        self.radio1_data_type = tk.Radiobutton(self.frame_data_type, text=const_GUI.data_type_1,
                                               variable=self.selected_data_type, value=const_GUI.data_type_1,
                                               indicatoron=0, width=30, command=self.change_lwc_attribute)
        self.radio2_data_type = tk.Radiobutton(self.frame_data_type, text=const_GUI.data_type_2,
                                               variable=self.selected_data_type, value=const_GUI.data_type_2,
                                               indicatoron=0, width=30, command=self.change_lwc_attribute)
        self.radio3_data_type = tk.Radiobutton(self.frame_data_type, text=const_GUI.data_type_3,
                                               variable=self.selected_data_type, value=const_GUI.data_type_3,
                                               indicatoron=0, width=30, command=self.change_lwc_attribute)
        self.radio4_data_type = tk.Radiobutton(self.frame_data_type, text=const_GUI.data_type_4,
                                               variable=self.selected_data_type, value=const_GUI.data_type_4,
                                               indicatoron=0, width=30, command=self.change_lwc_attribute)
        self.radio5_data_type = tk.Radiobutton(self.frame_data_type, text=const_GUI.data_type_5,
                                               variable=self.selected_data_type, value=const_GUI.data_type_5,
                                               indicatoron=0, width=30, command=self.change_lwc_attribute)
        self.radio6_data_type = tk.Radiobutton(self.frame_data_type, text=const_GUI.data_type_6,
                                               variable=self.selected_data_type, value=const_GUI.data_type_6,
                                               indicatoron=0, width=30, command=self.change_lwc_attribute)
        self.selected_data_type.set(value=const_GUI.data_type_1)

        # Define the data type widget's layout (1=Icing Prob. 2=LWC, 3=RH, 4=RHP, 5=T, 6=TP)
        self.data_type_label.grid(row=0, column=0)
        self.radio1_data_type.grid(row=1, column=0)
        self.radio2_data_type.grid(row=2, column=0)
        self.radio3_data_type.grid(row=3, column=0)
        self.radio4_data_type.grid(row=4, column=0)
        self.radio5_data_type.grid(row=5, column=0)
        self.radio6_data_type.grid(row=6, column=0)

        # Define the cross section widgets (1=GPH, 2=Lat, 3=Lon)
        self.cross_section_label = tk.Label(self.frame_cross_section, text=const_GUI.cross_section_label)
        self.selected_cross_section = tk.StringVar()
        self.radio1_cross_section = tk.Radiobutton(self.frame_cross_section, text=const_GUI.cross_section_1,
                                                   variable=self.selected_cross_section, value=const_GUI.cross_section_1,
                                                   indicatoron=0, width=10, command=self.change_cs_attributes_state)
        self.radio2_cross_section = tk.Radiobutton(self.frame_cross_section, text=const_GUI.cross_section_2,
                                                   variable=self.selected_cross_section, value=const_GUI.cross_section_2,
                                                   indicatoron=0, width=10, command=self.change_cs_attributes_state)
        self.radio3_cross_section = tk.Radiobutton(self.frame_cross_section, text=const_GUI.cross_section_3,
                                                   variable=self.selected_cross_section, value=const_GUI.cross_section_3,
                                                   indicatoron=0, width=10, command=self.change_cs_attributes_state)
        self.selected_cross_section.set(value=const_GUI.cross_section_1)

        # Define the data type widget's layout (1=GPH, 2=Lat, 3=Lon)
        self.cross_section_label.grid(row=0, column=0)
        self.radio1_cross_section.grid(row=1, column=0)
        self.radio2_cross_section.grid(row=2, column=0)
        self.radio3_cross_section.grid(row=3, column=0)

        # Define the cs attributes widgets
        self.hPa_level_label = tk.Label(self.frame_cs_attributes, text=const_GUI.hPa_level_label)
        self.hPa_level_list = [str(x) for x in range(100, 1001, 50)]

        self.selected_hPa_level = tk.StringVar()
        self.selected_hPa_level.set(const_GUI.default_hPa_level)
        self.optionmenu_hPa_level = tk.OptionMenu(self.frame_cs_attributes, self.selected_hPa_level, *self.hPa_level_list)

        self.cs_latlon_label = tk.Label(self.frame_cs_attributes, text=const_GUI.latlon_label_1, state=tk.DISABLED)
        self.cs_latlon_var = tk.StringVar()
        self.cs_latlon_var.set(const_GUI.default_lanlot_cs)
        self.cs_latlon_entry = tk.Entry(self.frame_cs_attributes, textvariable=self.cs_latlon_var, width=6, state=tk.DISABLED)

        self.cs_min_lwc_label = tk.Label(self.frame_cs_attributes, text=const_GUI.min_LWC_label)
        self.cs_min_lwc_var = tk.StringVar()
        self.cs_min_lwc_var.set(const_GUI.default_min_LWC)
        self.cs_min_lwc_entry = tk.Entry(self.frame_cs_attributes, textvariable=self.cs_min_lwc_var, width=6)

        self.files_path_label = tk.Label(self.frame_cs_attributes, text=const_GUI.files_path_label)
        self.files_path_var = tk.StringVar()
        self.files_path_var.set(const_GUI.default_file_path)
        self.files_path_entry = tk.Entry(self.frame_cs_attributes, textvariable=self.files_path_var)

        self.date_label = tk.Label(self.frame_cs_attributes, text=const_GUI.date_label)
        self.year_label = tk.Label(self.frame_cs_attributes, text=const_GUI.year_label)
        self.month_label = tk.Label(self.frame_cs_attributes, text=const_GUI.month_label)
        self.day_label = tk.Label(self.frame_cs_attributes, text=const_GUI.day_label)
        self.hour_label = tk.Label(self.frame_cs_attributes, text=const_GUI.hour_label)
        self.year_list = [str(x) for x in range(1996, 2017)]
        self.year_var = tk.StringVar()
        self.year_var.set(const_GUI.default_year)
        self.year_entry = tk.OptionMenu(self.frame_cs_attributes, self.year_var, *self.year_list)
        self.month_list = ["%02d" % x for x in range(1, 13)]
        self.month_var = tk.StringVar()
        self.month_var.set(const_GUI.default_month)
        self.month_entry = tk.OptionMenu(self.frame_cs_attributes, self.month_var, *self.month_list)
        self.day_list = ["%02d" % x for x in range(1, 32)]
        self.day_var = tk.StringVar()
        self.day_var.set(const_GUI.default_day)
        self.day_entry = tk.OptionMenu(self.frame_cs_attributes, self.day_var, *self.day_list)
        self.hour_list = ["%02d" % x for x in range(0, 19, 6)]
        self.hour_var = tk.StringVar()
        self.hour_var.set(const_GUI.default_hour)
        self.hour_entry = tk.OptionMenu(self.frame_cs_attributes, self.hour_var, *self.hour_list)

        self.detached_map = tk.IntVar()
        self.detached_map.set(0)
        self.detached_map_checkbutton = tk.Checkbutton(self.frame_cs_attributes, variable=self.detached_map,
                                                       text=const_GUI.detached_map_checkbutton)

        self.draw_button = tk.Button(self.frame_cs_attributes, text=const_GUI.draw_button_text, command=self.draw_map)

        # Define the cs attributes layout
        self.hPa_level_label.grid(row=0, column=0)
        self.optionmenu_hPa_level.grid(row=1, column=0)

        self.cs_latlon_label.grid(row=2, column=0)
        self.cs_latlon_entry.grid(row=3, column=0)

        self.cs_min_lwc_label.grid(row=4, column=0)
        self.cs_min_lwc_entry.grid(row=5, column=0)

        self.files_path_label.grid(row=0, column=1, padx=10)
        self.files_path_entry.grid(row=1, column=1, padx=10)

        self.date_label.grid(row=0, column=2, columnspan=3)
        self.year_label.grid(row=1, column=2)
        self.month_label.grid(row=1, column=3)
        self.day_label.grid(row=1, column=4)
        self.hour_label.grid(row=1, column=5)
        self.year_entry.grid(row=2, column=2)
        self.month_entry.grid(row=2, column=3)
        self.day_entry.grid(row=2, column=4)
        self.hour_entry.grid(row=2, column=5)

        self.detached_map_checkbutton.grid(row=3, column=1)

        self.draw_button.grid(row=5, column=1)

    def change_cs_attributes_state(self):
        if self.selected_cross_section.get() == const_GUI.cross_section_1:  # Enable hPa levels, disable latlon entry
            hPa_state = tk.NORMAL
            latlon_state = tk.DISABLED
            self.cs_latlon_label.config(text=const_GUI.latlon_label_1)
        else:  # Disable hPa levels, enable latlon entry
            hPa_state = tk.DISABLED
            latlon_state = tk.NORMAL
            if self.selected_cross_section.get() == const_GUI.cross_section_2:
                # Latitude
                self.cs_latlon_label.config(text=const_GUI.latlon_label_2)
            else:
                # Longitude
                self.cs_latlon_label.config(text=const_GUI.latlon_label_3)

        self.hPa_level_label.config(state=hPa_state)
        self.optionmenu_hPa_level.config(state=hPa_state)
        self.cs_latlon_entry.config(state=latlon_state)
        self.cs_latlon_label.config(state=latlon_state)

    def change_lwc_attribute(self):
        if self.selected_data_type.get() == const_GUI.data_type_1:
            # data type is Icing Probability
            lwc_state = tk.NORMAL
        else:
            lwc_state = tk.DISABLED
        self.cs_min_lwc_label.config(state=lwc_state)
        self.cs_min_lwc_entry.config(state=lwc_state)

    def draw_map(self):
        # The first part is completely done by matplotlib, and then transferred to Tkinter
        req_map_grid, lats, lons, vmin, vmax = calculate_icing_probability(self.selected_data_type.get(), self.selected_cross_section.get(),
                                                                           self.selected_hPa_level.get(), float(self.cs_latlon_var.get()),
                                                                           float(self.cs_min_lwc_var.get()), self.files_path_var.get(),
                                                                           self.year_var.get(), self.month_var.get(), self.day_var.get(),
                                                                           self.hour_var.get())
        map_figure = Figure(figsize=(8, 6.4), dpi=100)
        map_axis = map_figure.add_subplot(111)

        my_map = Basemap(projection='mill',
                         lat_0=lats.min(),
                         lon_0=lons.min(),
                         lat_1=lats.max(),
                         lon_1=lons.max(),
                         llcrnrlat=lats.min(), urcrnrlat=lats.max(),
                         llcrnrlon=lons.min(), urcrnrlon=lons.max(),
                         ax=map_axis
                         )
        lon, lat = np.meshgrid(lons, lats)
        xi, yi = my_map(lon, lat)
        cs = my_map.pcolor(xi, yi, np.squeeze(req_map_grid), vmin=vmin, vmax=vmax)

        # Add Grid Lines and Geography
        if self.selected_cross_section.get() == const_GUI.cross_section_1:
            # hPa level
            my_map.drawparallels(np.arange(lats.min(), lats.max(), 10.), labels=[1, 0, 0, 0], fontsize=8)
            my_map.drawmeridians(np.arange(lons.min(), lons.max(), 10.), labels=[0, 0, 0, 1], fontsize=8)
            # Add Coastlines, States, and Country Boundaries
            # my_map.fillcontinents()
            my_map.drawcoastlines()
            my_map.drawstates()
            my_map.drawcountries()
        elif self.selected_cross_section.get() == const_GUI.cross_section_2:
            # Latitude
            my_map.drawparallels(np.arange(lats.min(), lats.max(), 2.), labels=[1, 0, 0, 0], fontsize=8,
                                 fmt=(lambda x: u"%g hPa" % ((18-x)*50+100)))
            my_map.drawmeridians(np.arange(lons.min(), lons.max(), 10.), labels=[0, 0, 0, 1], fontsize=8)
        else:
            # Longitude
            my_map.drawparallels(np.arange(lats.min(), lats.max(), 10.), labels=[1, 0, 0, 0], fontsize=8)
            meridians = my_map.drawmeridians(np.arange(lons.min(), lons.max(), 2.), labels=[0, 0, 0, 1], fontsize=8,
                                             fmt=(lambda x: u"%g hPa" % (x*50+100)))
            for m in meridians:
                meridians[m][1][0].set_rotation(90)

        # Add Colorbar
        cbar = map_figure.colorbar(cs, orientation='horizontal')
        selected_data_type = self.selected_data_type.get()
        if selected_data_type == const_GUI.data_type_1 or selected_data_type == const_GUI.data_type_3\
                or selected_data_type == const_GUI.data_type_4 or selected_data_type == const_GUI.data_type_6:
            # Icing Prob, RH, RHP, TP
            cbar.set_label('Percents')
        elif selected_data_type == const_GUI.data_type_2:
            # LWC
            cbar.set_label('g/m3')
        else:
            # Temperature
            cbar.set_label('Celsius')

        # Add Title
        map_figure.suptitle(selected_data_type)

        if self.detached_map.get() == 0:
            # The map is drawn inside the current GUI
            # Create the tk.DrawingArea
            canvas = FigureCanvasTkAgg(map_figure, master=self.frame_map)
            canvas.show()
            canvas.get_tk_widget().grid(row=0, column=0, sticky="nsew")

            # Add the toolbar in a different frame and remove the x,y coords from appearing
            for child in self.frame_nav_toolbar.winfo_children():
                child.destroy()
            map_axis.format_coord = lambda x, y: ''
            toolbar = NavigationToolbar2TkAgg(canvas, self.frame_nav_toolbar)
            toolbar.update()
            # canvas._tkcanvas.grid(row=1, column=0)
        else:
            # The map is drawn in a seperate window
            main_seperate = tk.Tk()
            main_seperate.wm_title(selected_data_type)

            map_figure.add_subplot(111)

            # a tk.DrawingArea
            canvas = FigureCanvasTkAgg(map_figure, master=main_seperate)
            canvas.show()
            canvas.get_tk_widget().pack(side=tk.TOP, fill=tk.BOTH, expand=1)

            toolbar = NavigationToolbar2TkAgg(canvas, main_seperate)
            toolbar.update()
            canvas._tkcanvas.pack(side=tk.TOP, fill=tk.BOTH, expand=1)


root = tk.Tk()
my_gui = Icing_Probability_GUI(root)
root.mainloop()

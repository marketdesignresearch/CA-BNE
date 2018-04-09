#!/usr/bin/python3

import os
import sys
import glob
import re
import math

import matplotlib as mpl

from mpl_toolkits.mplot3d import Axes3D
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as anim

import itertools
import time


class MySubplot(object):
    def __init__(self, data, ax, title, zlim):
        self.data = data
        self.ax = ax
        self.title = title
        self.zlim = zlim
        
        self.x = {}
        self.y = {}
        for k,v in data.items():
            n = len(v)
            self.x[k] = np.kron(np.ones((1,n)), np.matrix(np.linspace(zlim[0], zlim[1], n)).T)
            self.y[k] = self.x[k].T
        
        # parameters are elevation and azimuth (in degrees, not radians)
        ax.view_init(None, -135)

    def anim_update(self, i):
        self.ax.clear()
        self.ax.set_zlim(self.zlim)
        self.ax.set_title(self.title)
        self.ax.plot_wireframe(self.x[i], self.y[i], self.data[i])
        
        

class Main(object):
    data = {}
    titles = ["Local Left", "Local Right", "Global Left", "Global Right"]
    zlims = [(0,1),(0,1),(0,2),(0,2)]

    def __init__(self, path, *sliceargs):
        suffix = "strats"
        if len(sliceargs) == 0:
            self.slice = slice(9999999)
        else: 
            self.slice = slice(*[int(x) for x in sliceargs])
        if 0:
            # plot utilitylosses instead of strats
            suffix = "utilitylosses"
            self.zlims = [(0,0.01),(0,0.01),(0,0.01),(0,0.01)]
            if len(sliceargs) == 0:
                self.slice = slice(1, 9999999)
            else: 
                self.slice = slice(*[max(1, int(x)) for x in sliceargs])
            
    
            
            
        #read in data
        strategy_files = [os.path.join(path, f) for f in os.listdir(path) 
                          if re.fullmatch("iter\d*\." + suffix, f)]
        print(strategy_files)
        for f in strategy_files:
            self.parse_strategy_file(f)
        
        self.anim()
            
            
    def parse_strategy_file(self, path):
        players = []
    
        with open(path) as fd:
            lines = iter(fd)
            #read header line
            iteration, = re.match('''"Iteration","(\d+)"''', next(lines)).groups()
            
            #iteration, = re.match(".*?iter(\d+)\\.txt\\.epsilon", path).groups()
            #next(lines)

            player = None
            for line in lines:
                numbers = re.findall('''\d+\.\d+''', line)
                if not numbers:
                    # we are at a new player
                    player = line.strip('"')
                    players.append([])
                    continue
                players[-1].append([float(i) for i in numbers])
            # add data for global player's other strategy
            players.append(list(zip(*players[-1])))
       
        self.data[int(iteration)] = players
        
    def anim(self):
        myplots = []
        fig = plt.figure(figsize=(16,12))
        
        for i in range(4):
            sp = MySubplot(
                data={k:v[i] for k,v in self.data.items()}, 
                ax=fig.add_subplot(221 + i, projection='3d'), 
                title=self.titles[i],
                zlim=self.zlims[i],
            )
            myplots.append(sp)
        
        frames = range(max(self.data.keys())+1)[self.slice]
        print(frames)

        def anim_update(i):
            i = min(i, len(frames)-1)
            i = frames[i]
            for k in myplots:
                k.anim_update(i)
    
        #a = anim.FuncAnimation(fig, anim_update, frames=len(frames) + 5, repeat=True, interval=300)
        a = anim.FuncAnimation(fig, anim_update, frames=len(frames) + 5, repeat=True, interval=500)
        plt.show()


Main(*sys.argv[1:])



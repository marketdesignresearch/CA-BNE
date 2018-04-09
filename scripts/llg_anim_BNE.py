#!/usr/bin/python3

import os
import sys
import glob
import re
import math

import matplotlib as mpl

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as anim

import itertools
import time


def main(path):
    plot_BNE(path)
        
        

def plot_BNE(path):
    data = []
    max_iter = 0

    with open(path) as fd:
        for line in fd.readlines():
            iteration, time_ms, *xy = line.split()
            iteration = int(iteration)
            #time_ms = int(time_ms)
            
            itr = iter(xy)
            xy_pairs = [(float(x), float(y)) for (x,y) in zip(itr, itr)]
            if len(xy_pairs) == 0:
                xx, yy = [0,1], [.5,.5]
            else:
                xx, yy = zip(*xy_pairs)
            data.append((xx,yy))
            max_iter = max(max_iter, iteration)

    
    fig = plt.figure(figsize=(12,12))
    
    def anim_update(i):
        fig.clear()
        fig.suptitle("LLG")
        plt.xlim(0.0, 1.0)
        plt.ylim(0.0, 1.0)
        
        plt.xticks(np.arange(0, 1.01, 0.05))
        plt.yticks(np.arange(0, 1.01, 0.05))
        
        xx, yy = data[i]
        plt.plot(xx, yy, "-", clip_box=mpl.transforms.Bbox([[0,0],[0.1,0.3]]), clip_on=True)
        #plt.plot(xx, yy, ".-")
        
        # analytical BNE
        #xx, yy = data[-1]
        #plt.plot(xx, yy, "-")
         
    a = anim.FuncAnimation(fig, anim_update, frames=max_iter + 1, repeat=False, interval=300)
    plt.show()


for path in sys.argv[1:]:
    main(path)


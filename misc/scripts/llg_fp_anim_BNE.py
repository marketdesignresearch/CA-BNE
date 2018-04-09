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
            iteration, time_ms, *xyz = line.split()
            iteration = int(iteration)
            #time_ms = int(time_ms)
            
            itr = iter(xyz)
            triples = [(float(x), float(y), float(z)) for (x,y,z) in zip(itr, itr, itr)]
            
            xx, yy, zz = zip(*triples)
            data.append((xx, yy, zz))
            max_iter = max(max_iter, iteration)

    
    fig = plt.figure(figsize=(12,12))
    
    def anim_update(i):
        fig.clear()
        fig.suptitle("LLG First Price")
        plt.xlim(0.0, 2.0)
        plt.ylim(0.0, 2.0)
        
        plt.xticks(np.arange(0, 2.01, 0.1))
        plt.yticks(np.arange(0, 2.01, 0.1))
        
        xx, yy, zz = data[i]
        plt.plot(xx[:len(xx)//2], yy[:len(xx)//2], "-", clip_box=mpl.transforms.Bbox([[0,0],[0.1,0.3]]), clip_on=True, label="Local Players")
        plt.plot(xx, zz, "-", clip_box=mpl.transforms.Bbox([[0,0],[0.1,0.3]]), clip_on=True, label="Global Player")
        #plt.plot(xx, yy, ".-")
        
        # analytical BNE
        #xx, yy = data[-1]
        #plt.plot(xx, yy, "-")
        
        plt.legend(loc='upper left')
        

    a = anim.FuncAnimation(fig, anim_update, frames=max_iter + 1, repeat=False, interval=300)
    plt.show()


for path in sys.argv[1:]:
    main(path)


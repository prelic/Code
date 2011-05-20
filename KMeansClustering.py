#!/usr/bin/env python
import random
import numpy
import math

class Cluster:
    points = []
    centers = []
    membership = []
    num_centers = 0
    def __init__(self, k, pts):
        
        num_centers = k
        for i in range(pts):
            x = random.randint(0,100)
            y = random.randint(0,100)
            self.points.append([x,y])

        for i in range(k):
            x = random.randint(0,100)
            y = random.randint(0,100)
            self.centers.append([x,y])

    #calculates distances between each point and each center,
    #chooses the smallest distance center for membership
    def calculateCenters(self):
        self.membership = []
        temp = []
        for [x,y] in self.points:
            for [a,b] in self.centers:
                temp.append(math.sqrt((x-a)**2+(y-b)**2)) 
            self.membership.append(numpy.argmin(temp))
            temp = []

    #calculates the new centers       
    def moveCenters(self):
        newcenters = []
        point_counts = []
        for i in self.centers:
            newcenters.append([0,0])
            point_counts.append(0)
        
        newcenters = numpy.array(newcenters)
        self.points = numpy.array(self.points)
        
        #sum points of each class
        for i in range(len(self.points)):
            newcenters[self.membership[i],]+=self.points[i,]
            
        #calculate how many points for each class    
        for i in range(len(self.membership)):
            point_counts[self.membership[i]]+=1
            
        #divide sum points by number points for each class
        #to get the average (aka, new center)
        for i in range(len(point_counts)):
            newcenters[i]=newcenters[i]/point_counts[i]
        self.centers = newcenters
        
        


c = Cluster(5, 100)
for i in range(20):
    c.calculateCenters()
    c.moveCenters()
print c.centers
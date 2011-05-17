#! /usr/bin/env python
import webbrowser
import subprocess
import random
import urllib
from xml.dom import minidom
import time

#dictionary containing valid rules
#	key: 	rule name
#	value:	method associated with that rule
class knowledge:
	"""knowledge database"""
	
	#declare the rules dictionary (center)
	def __init__(self):
		self.center = {}
		self.notifications = [[1,0,0,0,0, "this is a list"]]
	
	#add the rule and action to the dictionary:
	#	-rule: name of the rule subclass
	#	-action: member method to be executed (minus params) from the class given in param 1
	def add(self, rule, action):
		self.center[str.upper(str(rule.rule_name))] = action

	#prints the rules in the knowledge base
	def printKnowledgeCenter(self):
		print "\n\nKNOWLEDGE CENTER: \n-----------------\n"
		for rules in sorted(self.center):
			print "RULE: ",rules,"\n"

	#adds all the rules into the database
	#should only be called once per run
	#future: automate? 
	def buildKnowledgeCenter(self):
	
		#google search
		self.add(GOOGLE, GOOGLE().SEARCH)
		
		#go to specific url
		self.add(WEBSITE, WEBSITE().GO)
		
		#open an application
		self.add(APP, APP().LAUNCH)
		
		self.add(COINFLIP, COINFLIP().LAUNCH)
		
		self.add(WEATHER, WEATHER().LAUNCH)


	#execute the rule given by rule, params specified with the string params
	def executeRule(self, rule, params):
		self.center[rule](str(params))
	
	#returns a sorted list of the rule names
	def getRules(self):
		return sorted(self.center)
#------------------------------------------------------------			

class rule:
	"""actions"""
		
class GOOGLE (rule):
			
	rule_name = 'GOOGLE'
	
	def SEARCH(self, keywords):
		webbrowser.open("http://www.Google.com/search?q="+str(keywords))
	
class WEBSITE (rule):
	rule_name = "WEBSITE"
	
	def GO(self, url):
		webbrowser.open("http://www."+url)

class APP (rule):
	rule_name = "APP"
	
	def LAUNCH(self, keywords):
		subprocess.call(["open","/Applications/"+str(keywords)+".app"])

class COINFLIP (rule):
	rule_name = "COINFLIP"
	
	def LAUNCH(self, keywords):
		i = random.random()
		if i < .5:
			print "Heads"
		else:
			print "Tails"
			
class WEATHER (rule):
	rule_name = "WEATHER"
	URL = "http://xml.weather.yahoo.com/forecastrss?p=%s"
	NS = "http://xml.weather.yahoo.com/ns/rss/1.0"
	def LAUNCH(self, keywords):
		url = self.URL % keywords
		dom = minidom.parse(urllib.urlopen(url))
		forecast = []
		for n in dom.getElementsByTagNameNS(self.NS, "forecast"):
			forecast.append({
				"date": n.getAttribute("date"),
				"low": n.getAttribute("low"),
				"high": n.getAttribute("high"),
				"condition": n.getAttribute("text")
			})
		ycondition = dom.getElementsByTagNameNS(self.NS, "condition") [0]
		title = dom.getElementsByTagName("title")[0].firstChild.data
		title = title.replace("Yahoo! Weather - ","")
		print "Title: ", title
		print "Temp: ", ycondition.getAttribute("temp")
		print "Forecast:",ycondition.getAttribute("text")
		

#------------------------------------------------------------			

class phrase:
	"""Parsed phrases"""
	
	def __init__(self, action, (preaction, object)):
		self.action = str.upper(str(action))
		self.value = (str.upper(str(preaction)), str.upper(str(object)))		
		print self.value
		
	def executePhrase():
		pass

#string -> Phrase |
#string -> Command |
#string -> Output
def parseInput(input):
	input
	
	if input == "PRINT RULES":
		k.printKnowledgeCenter()
	else: 
		input_list = input.split(" ")
		result = tryMatchToRule(input_list)
		if result == "none":
			print "Couldn't find matching rule\n"
		else:
			#result index is the index of the rule relative to the sentence
			result_index = input_list.index(result)	
			
			#execute the action specified by the found rule, with the param list being the rest of the sentence
			k.executeRule(str.upper(input_list[result_index]), " ".join(input_list[result_index+1:]))
		
#return
def tryMatchToRule(input_list):
	#get list of rules
	rules = k.getRules()
	result = "none"
	#for each word in the phrase
	for words in input_list:
		#if length of the word is bigger than 3 
		if len(words) >= 3:
			try:
				#if it exists
				rules.index(str.upper(words))
				result = words
				break
			except ValueError:
				result = "none"
	return result
		

def checkEvents():
	c_time = time.localtime()
	time_t = [c_time[3],c_time[4], c_time[5], c_time[1], c_time[0]]
	for n in k.notifications:
		if time_t[0] > n[0] and time_t[1] > n[1] and time_t[2] > n[2] and time_t[3] > n[3] and time_t[4] > n[4]:
			print n[5]
			k.notifications.remove(n)
			break

	
#------------------------------------------------------------			

k = knowledge()
k.buildKnowledgeCenter()

input = ""
while True:
	checkEvents()
	input = raw_input("sam:")
	if input == "quit":
		print "\nHave a nice day"
		print k.notifications[0][0]
		break;
	else:
		parseInput(input)

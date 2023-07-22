import requests
from bs4 import BeautifulSoup

#beach class
class Beach:
    #default constructor
    def __init__(beach):
        beach.bio="bio"
        beach.weather="weather"
        beach.tide="tide"
        beach.buoy="buoy"
        beach.temp="temp"

    #constructor
    def fill(beach,bio,weather,tide,buoy,temp):
        beach.bio=bio
        beach.weather=weather
        beach.tide=tide
        beach.buoy=buoy
        beach.temp=temp
    
#get data from webpage and add to beach element
def getBeachData(url, name):
        page=requests.get(url)
        soup=BeautifulSoup(page.content, "html.parser")
        data=soup.find(id="fcst-current-data")
        elements=data.find_all("div", class_="current-data-desc")
        name=Beach.fill(name, "filler", elements[0].text.strip(), elements[1].text.strip(), elements[2].text.strip(), elements[3].text.strip().split("\n")[0])
        
#testing
santaBarbara = Beach()
getBeachData("https://surfcaptain.com/forecast/santa-barbara-california", santaBarbara)
print(santaBarbara.weather)
print(santaBarbara.tide)
print(santaBarbara.buoy)
print(santaBarbara.temp)

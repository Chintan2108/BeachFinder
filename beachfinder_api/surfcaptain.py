import requests
from bs4 import BeautifulSoup

class Beach:
    def __init__(beach):
        beach.bio="bio"
        beach.weather="weather"
        beach.tide="tide"
        beach.buoy="buoy"
        beach.temp="temp"

    def fill(beach,bio,weather,tide,buoy,temp):
        beach.bio=bio
        beach.weather=weather
        beach.tide=tide
        beach.buoy=buoy
        beach.temp=temp
    
def getBeachData(url, name):
        page=requests.get(url)
        soup=BeautifulSoup(page.content, "html.parser")
        data=soup.find(id="fcst-current-data")
        elements=data.find_all("div", class_="current-data-desc")
        name=Beach.fill(name, "filler", elements[0].text.strip(), elements[1].text.strip(), elements[2].text.strip(), elements[3].text.strip().split("\n")[0])
        

santaBarbara = Beach()
getBeachData("https://surfcaptain.com/forecast/santa-barbara-california", santaBarbara)
print(santaBarbara.weather)
print(santaBarbara.tide)
print(santaBarbara.buoy)
print(santaBarbara.temp)
#Santa Barbara
""" sb_url ="https://surfcaptain.com/forecast/santa-barbara-california"
sb_page = requests.get(sb_url)

sb_soup = BeautifulSoup(sb_page.content, "html.parser")

sb_data=sb_soup.find(id="fcst-current-data")
    #print (sb_data.prettify)

sb_elements =  sb_data.find_all("div", class_= "current-data-desc")

sb = Beach("filler", sb_elements[0].text.strip(), sb_elements[1].text.strip(), sb_elements[2].text.strip(), sb_elements[3].text.strip())
print (sb.weather)
print (sb.tide)
print (sb.buoy)
print (sb.temp.split("\n")[0]) """
#Laguna 

#Venice

#La Jolla

#Huntington

#Malibu
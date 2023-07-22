'''Contains class definition for the 'score' endpoint'''

from flask_restful import Resource
from flask import request
import requests
from bs4 import BeautifulSoup

def get_surfcaptain_beach_data(beach_name):
        page=requests.get(f"https://surfcaptain.com/forecast/{beach_name}")
        soup=BeautifulSoup(page.content, "html.parser")
        data=soup.find(id="fcst-current-data")
        elements=data.find_all("div", class_="current-data-desc")
        return (elements[2].text.strip().split('@')[0], elements[3].text.strip().split("\n")[0], elements[0].text.strip()[4:])

def get_coastal_ca_data(record_id):
    response = requests.get(f'https://api.coastal.ca.gov/access/v1/locations/id/{record_id}')
    response = response.json()
    return (response[0]['FEE'], response[0]['PARKING'], response[0]['DSABLDACSS'], response[0]['RESTROOMS'])

class BeachScore(Resource):
    '''
    Class for scoring beaches based on location and criteria
    '''
    def get(self):
        beach_name = request.args.get('beach')

        metadata = {
            'huntington-beach-california': ['Huntington Beach', 1255],
            'laguna-beach-california': ['Laguna Beach', 1304],
            'santa-barbara-california': ['Santa Barbara', 897],
            'la-jolla-california': ['La Jolla Beach', 1450],
            'malibu-california': ['Malibu Beach', 1057],
            'venice-beach-california': ['Venice Beach', 1091],
        }

        beach_info_response = {
            'beach_name': metadata[beach_name][0],
            'water_level': None,
            'water_temp': None,
            'wind': None,
            'fee': None,
            'parking': None,
            'disabled_access': None,
            'restrooms': None
        }

        surfcaptain_data = get_surfcaptain_beach_data(beach_name)
        coastal_ca_data = get_coastal_ca_data(metadata[beach_name][1])

        beach_info_response['water_level'] = surfcaptain_data[0]
        beach_info_response['water_temp'] = surfcaptain_data[1]
        beach_info_response['wind'] = surfcaptain_data[2]
        beach_info_response['fee'] = coastal_ca_data[0]
        beach_info_response['parking'] = coastal_ca_data[1]
        beach_info_response['disabled_access'] = coastal_ca_data[2]
        beach_info_response['restrooms'] = coastal_ca_data[3]
        
        return beach_info_response, 200
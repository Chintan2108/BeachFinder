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
            'huntingtonbeach': ['Huntington Beach', 'https://assets.simpleviewinc.com/simpleview/image/upload/c_limit,h_1200,q_75,w_1200/v1/clients/surfcityusa/DJI_0387_1__2a9cbdab-8c22-4a75-bc70-ff743394f11d.jpg', 'huntington-beach-california', 1255],
            'lagunabeach': ['Laguna Beach', 'https://cdn.britannica.com/69/175869-050-DFF34225/Crescent-Bay-Beach-Laguna-California.jpg', 'laguna-beach-california', 1304],
            'santabarbarabeach': ['Santa Barbara', 'https://beachsideinn.com/wp-content/uploads/2021/03/IMG_7459-1024x679-1.jpg', 'santa-barbara-california', 897],
            'lajollabeach': ['La Jolla Beach', 'https://lajollamom.com/wp-content/uploads/2018/05/la-jolla-cove-guide.jpg', 'la-jolla-california', 1450],
            'malibubeach': ['Malibu Beach', 'https://malibuluxuryrealty.com/wp-content/uploads/2019/05/Latigo-Beach-palm-trees-Malibu-1-1.jpg', 'malibu-california', 1057],
            'venicebeach': ['Venice Beach', 'https://a.cdn-hotels.com/gdcs/production103/d1593/995f6282-43fe-464d-ba3d-2b646a8f7ec3.jpg', 'venice-beach-california', 1091],
        }

        beach_info_response = {
            'beach_name': metadata[beach_name][0],
            'beach_image_uri': metadata[beach_name][1],
            'water_level': None,
            'water_temp': None,
            'wind': None,
            'fee': None,
            'parking': None,
            'disabled_access': None,
            'restrooms': None
        }

        surfcaptain_data = get_surfcaptain_beach_data(metadata[beach_name][2])
        coastal_ca_data = get_coastal_ca_data(metadata[beach_name][3])

        beach_info_response['water_level'] = surfcaptain_data[0]
        beach_info_response['water_temp'] = surfcaptain_data[1]
        beach_info_response['wind'] = surfcaptain_data[2]
        beach_info_response['fee'] = coastal_ca_data[0]
        beach_info_response['parking'] = coastal_ca_data[1]
        beach_info_response['disabled_access'] = coastal_ca_data[2]
        beach_info_response['restrooms'] = coastal_ca_data[3]
        
        return beach_info_response, 200
'''Contains class definition for the 'score' endpoint'''

from flask_restful import Resource
from flask import request
import requests
from bs4 import BeautifulSoup
import pandas as pd

def get_algal_scores(beach_name):
    ref_url = 'https://raw.githubusercontent.com/Chintan2108/BeachFinder/main/Algal_Response_CA_Beaches.csv'
    temp_df = pd.read_csv(ref_url, index_col=0)
    algal_response = temp_df.to_dict()
    del temp_df
    return algal_response['Algal Score'][beach_name]

def get_surfcaptain_beach_data(beach_name):
    page=requests.get(f"https://surfcaptain.com/forecast/{beach_name}")
    soup=BeautifulSoup(page.content, "html.parser")
    data=soup.find(id="fcst-current-data")
    elements=data.find_all("div", class_="current-data-desc")
    return (elements[2].text.strip().split('@')[0], elements[3].text.strip().split("\n")[0]+'F', elements[0].text.strip()[4:], elements[0].text.strip()[:3]+'F')

def get_coastal_ca_data(record_id):
    response = requests.get(f'https://api.coastal.ca.gov/access/v1/locations/id/{record_id}')
    response = response.json()
    return (response[0]['FEE'], response[0]['PARKING'], response[0]['DSABLDACSS'], response[0]['RESTROOMS'])

def get_beach_score(beach_info):
    beach_score = 0
    
    # algal score (35%)
    beach_score += (1 - beach_info['algal_score'])*35

    # wind score (25%)
    wind_speed = float(beach_info['wind'][-4])
    if wind_speed <= 7:
        beach_score += 25
    if wind_speed >7 and wind_speed <= 12:
        beach_score += 10
    if wind_speed > 12:
        beach_score += 1
    
    # water temp (25%)
    water_temp = float(beach_info['water_temp'][:2])
    if water_temp > 68 and water_temp < 75:
        beach_score += 25
    if water_temp > 65 and water_temp <= 68:
        beach_score += 15
    if water_temp > 60 and water_temp <= 65:
        beach_score += 10
    if water_temp <= 60:
        beach_score += 1
    
    # air temp (15%)
    air_temp = float(beach_info['air_temp'][:2])
    if air_temp > 90:
        beach_score += 10
    if air_temp < 78:
        beach_score += 5
    if air_temp >= 78 and air_temp <= 90:
        beach_score += 15
    
    return int(beach_score/10)

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
            'air_temp': None,
            'wind': None,
            'fee': None,
            'parking': None,
            'disabled_access': None,
            'restrooms': None,
            'algal_score': None,
            'beach_score': None
        }

        surfcaptain_data = get_surfcaptain_beach_data(metadata[beach_name][2])
        coastal_ca_data = get_coastal_ca_data(metadata[beach_name][3])

        beach_info_response['water_level'] = surfcaptain_data[0]
        beach_info_response['water_temp'] = surfcaptain_data[1]
        beach_info_response['wind'] = surfcaptain_data[2]
        beach_info_response['air_temp'] = surfcaptain_data[3]
        beach_info_response['fee'] = coastal_ca_data[0]
        beach_info_response['parking'] = coastal_ca_data[1]
        beach_info_response['disabled_access'] = coastal_ca_data[2]
        beach_info_response['restrooms'] = coastal_ca_data[3]
        beach_info_response['algal_score'] = get_algal_scores(beach_name)
        
        # get overall beach score
        beach_info_response['beach_score'] = get_beach_score(beach_info_response)

        return beach_info_response, 200

get_surfcaptain_beach_data('huntington-beach-california')
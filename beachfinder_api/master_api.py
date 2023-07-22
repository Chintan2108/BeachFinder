from flask import Flask
from flask_restful import Resource, Api, reqparse
import pandas as pd
import ast
from scores import BeachScore

app = Flask(__name__)
api = Api(app)

api.add_resource(BeachScore, '/score')

if __name__ == '__main__':
    app.run(debug=True)  # run our Flask app

from flask import Flask, request, jsonify
import firebase_admin
from firebase_admin import credentials, firestore
from urllib.parse import urlparse


app = Flask(__name__)
# firestore creds
cred = credentials.Certificate('serviceAccountKey/bloom-book-8b2fe-firebase-adminsdk-2xjbv-2956e5c861.json')
firebase_admin.initialize_app(cred)


# Get a Firestore client
db = firestore.client()

#load the plants collection
collection_ref = db.collection('plants')
# Retrieve all documents in the collection
plantsObj = collection_ref.get()

plantNames = {plant.id: (plant.to_dict()['botanical_name'] if ('botanical_name' in  plant.to_dict().keys()) else plant.id) for plant in plantsObj}


def jaccard_similarity(x,y):
  """ returns the jaccard similarity between two lists """
  x = str(x).lower()
  y = str(y).lower()
  print((x,y))
  intersection_cardinality = len(set.intersection(*[set(x), set(y)]))
  union_cardinality = len(set.union(*[set(x), set(y)]))
  return intersection_cardinality/float(union_cardinality)


@app.route("/api/search_plants/<string:query>", methods=["GET"])
def api(query):
    query = str(query)
    # Split the request string by single quotes to get the URL part
    url_part = query.split("'")[1]

    # Split the URL part by slashes ('/') to get the query parameter
    query = url_part.split('/')[-1]

    # Find the index of the single quote before the [GET]
    quote_index = query.find("'")

    # Extract the word before the single quote
    query = query[:quote_index]

    query = query.replace("%20", "")

    search_results = []
    for plantID, scientific_name in plantNames.items():
       
        similarity_score_ID = jaccard_similarity(query, plantID)
        similarity_score_SN = jaccard_similarity(query, scientific_name)
        
        if query.lower() in plantID.lower(): #aloe, aloe vera
            similarity_score_ID = 0.99
        if query.lower() in scientific_name.lower(): 
            similarity_score_SN = 0.99
        if similarity_score_ID > 0.75 or similarity_score_SN > 0.75:
            similarity_score = similarity_score_ID if similarity_score_ID > similarity_score_SN else similarity_score_SN
            search_results.append({"plant": plantID, "scientific name": scientific_name, "similarity_score": similarity_score})
    
    # Sort search results based on similarity score
    search_results = sorted(search_results, key=lambda x: x["similarity_score"], reverse=True)

    return jsonify(search_results)



if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)

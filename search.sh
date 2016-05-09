curl -X POST \
  'http://localhost:8990/solr/icd10/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d 'stade terminal cancer épidermoïde pharyngo laryngé'

curl -X POST \
  'http://localhost:8990/solr/icd10/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d 'insuffisance rénale aigue'

curl -X POST \
  'http://localhost:8990/solr/icd10/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d 'diabète non insulino-dépendant avec complications vasculaires et coronariennes' 

curl -X POST \
  'http://localhost:8990/solr/icd10/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d 'Hyperttesnion artérielle'

curl -X POST \
  'http://localhost:8990/solr/filteredDictCorpus/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d 'CARDIOMYOPATHIE ISCHEMIQUE; HTA; CHUTES A REPETITION'

curl -X POST \
  'http://localhost:8990/solr/filteredDictCorpus/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d 'diabète avec complications'

curl -X POST \
  'http://localhost:8990/solr/filteredDictCorpus/tag?overlaps=NO_SUB&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d "Toxicomanie à l'héroïne"

curl -X POST \
  'http://localhost:8990/solr/filteredDictCorpus/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d "Syndrome du long QT"

curl -X POST \
  'http://localhost:8990/solr/filteredDictCorpus/tag?overlaps=LONGEST_DOMINANT_RIGHT&matchText=true&tagsLimit=5000&fl=uuid,icd10,term&wt=json&indent=on' \
  -H 'Content-Type:text/plain ; charset=utf-8' -d "Syndrome QT long"

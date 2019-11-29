from bs4 import BeautifulSoup
from bs4.element import Comment
import urllib.request
import sys

def tag_visible(element):
	if element.parent.name in ['style', 'script', 'head', 'title', 'meta', '[document]']:
		return False
	if isinstance(element, Comment):
		return False
	return True

def is_valid_question(question):
	question = question.lower()
	words = question.split(" ")
	if 'forgot' in words and len(words) <= 2:
		return False;
	return True;

def navigate_till_question(items, startIndex):
	if startIndex >= len(items) - 1:
		return len(items)
	for idx in range(startIndex, len(items)-1):
		item = items[idx]
		if len(item) > 0 and is_valid_question(item) and item[len(item)-1] == '?':
			return idx
	return len(items)

def extractAns(items, qIdx, q2Idx):
	ret = ''
	if q2Idx >= len(items) - 1:
		#end ans when we find first fullstop
		for idx in range(qIdx, q2Idx):
			ret += items[idx] + ' '
			if "." in items[idx]:
				return ret
		return ret
			
	if qIdx == q2Idx:
		return items[qIdx]
	
	for idx in range(qIdx, q2Idx):
		ret += items[idx] + ' '
	return ret

def text_from_html(body):
	soup = BeautifulSoup(body, 'html.parser')
	texts = soup.findAll(text=True)
	visible_texts = filter(tag_visible, texts)
	#print(visible_texts)
	items = []
	
	for text in visible_texts:
		items.append(text.strip())
	#print(len(items))
#	print(items[navigate_till_question(items, 0)])
    #return u" ".join(t.strip() for t in visible_texts)

	idx = 0
	while True:
		qIdx = navigate_till_question(items, idx)
		q2Idx = navigate_till_question(items, qIdx+1)
		ans = extractAns(items, qIdx + 1, q2Idx - 1)
		qna[items[qIdx].encode("ascii", errors="ignore").decode()] = ans.encode("ascii", errors="ignore").decode()
		#print(str(qIdx) + ':' + str(q2Idx))
		if q2Idx == len(items):
			break
		else:
			idx = q2Idx
	
qna = {}
url_input = sys.argv[1]
#print(url_input)
html = urllib.request.urlopen(url_input).read()
#html = urllib.request.urlopen('https://help.twitter.com/en/safety-and-security/public-and-protected-tweets').read()
#html = urllib.request.urlopen('https://www.amazon.com/Alexa-Privacy-Hub/b?ie=UTF8&node=19149165011').read()
#print(text_from_html(html))
text_from_html(html)
#print('parsed content:')
#print(len(qna))
print(qna)
#for key, value in qna.items() :
	#print ('Q: ' + key)
	#print ('Ans: ' + value)

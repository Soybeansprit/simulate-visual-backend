import snowboydecoder
import signal
import wave
import sys
import json
import requests
import time
import os
import base64
from pyaudio import PyAudio, paInt16
import webbrowser
from fetchToken import fetch_token
import time
import unicodedata
import math
import pickle
import Levenshtein
import cn2an
import pypinyin
import re
import retrieval

IS_PY3 = sys.version_info.major == 3
if IS_PY3:
    from urllib.request import urlopen
    from urllib.request import Request
    from urllib.error import URLError
    from urllib.parse import urlencode
    from urllib.parse import quote_plus
else:
    import urllib2
    from urllib import quote_plus
    from urllib2 import urlopen
    from urllib2 import Request
    from urllib2 import URLError
    from urllib import urlencode

interrupted = False  # snowboy监听唤醒结束标志
endSnow = False  # 程序结束标志

framerate = 16000  # 采样率
num_samples = 2000  # 采样点
channels = 1  # 声道
sampwidth = 2  # 采样宽度2bytes
pid = ''

music_exit = 'audio/exit.wav'  # 唤醒系统退出语音
music_open = 'audio/open.wav'  # 唤醒系统打开语音
music_alarm = 'audio/alarm.wav'
music_listen = 'audio/listen.wav'
music_over = 'audio/over.wav'

FILEPATH = 'audio/audio.wav'
MAPPATH = 'cemap.txt'
DEVICEPATH = 'my-case-study.properties'
SRC = 'ChineseRequirements.txt'
requirementTexts = ''

# os.close(sys.stderr.fileno())  # 去掉错误警告


def signal_handler(signal, frame):
    """
    监听键盘结束
    """
    global interrupted
    interrupted = True


def interrupt_callback():
    """
    监听唤醒
    """
    global interrupted
    return interrupted


def detected():
    """
    唤醒成功
    """
    print('唤醒成功')
    play(music_open)
    global interrupted
    interrupted = True
    detector.terminate()


def play(filename):
    """
    播放音频
    """
    wf = wave.open(filename, 'rb')  # 打开audio.wav
    p = PyAudio()  # 实例化 pyaudio
    # 打开流
    stream = p.open(format=p.get_format_from_width(wf.getsampwidth()),
                    channels=wf.getnchannels(),
                    rate=wf.getframerate(),
                    output=True)
    data = wf.readframes(1024)
    while data != b'':
        data = wf.readframes(1024)
        stream.write(data)
    # 释放IO
    stream.stop_stream()
    stream.close()
    p.terminate()


def save_wave_file(filepath, data):
    """
    存储文件
    """
    wf = wave.open(filepath, 'wb')
    wf.setnchannels(channels)
    wf.setsampwidth(sampwidth)
    wf.setframerate(framerate)
    wf.writeframes(b''.join(data))
    wf.close()

def my_record():
    """
    录音
    """
    pa = PyAudio()
    global requirementTexts
    text = ''
    TOKEN = fetch_token()  # 获取token
    while text != '再见。':
        stream = pa.open(format=paInt16, channels=channels, rate=framerate, input=True, frames_per_buffer=num_samples)
        my_buf = []
        print('开始录音...')
        for i in range(0, int(framerate / num_samples * 5)):
            string_audio_data = stream.read(num_samples)
            my_buf.append(string_audio_data)
            save_wave_file(FILEPATH, my_buf)
        speech = get_audio(FILEPATH)
        result = speech2text(speech, TOKEN, int(80001))
        text = result
        print(text)
        r = retrieval.Retrieval()
        text_modified = r.retrieve(text)
        print(text_modified)
        if text == '' : text = '再见。'
        if text != '再见。':
            try:
                eRequirement = c2e(text_modified).strip()
                print(eRequirement)
                play(music_listen)
            except Exception as e:
                pass
        stream.close()
    print('再见!')
    play(music_over)


def speech2text(speech_data, token, dev_pid=1537):
    """
    音频转文字
    """
    FORMAT = 'wav'
    RATE = '16000'
    CHANNEL = 1
    CUID = 'baidu_workshop'
    SPEECH = base64.b64encode(speech_data).decode('utf-8')
    data = {
        'format': FORMAT,
        'rate': RATE,
        'channel': CHANNEL,
        'cuid': CUID,
        'len': len(speech_data),
        'speech': SPEECH,
        'token': token,
        'dev_pid': dev_pid
    }

    # 语音转文字接口 该接口可能每个人不一样，取决于你需要哪种语音识别功能，本文使用的是 语音识别极速版

    url = 'https://vop.baidu.com/pro_api'
    headers = {'Content-Type': 'application/json'}  # 请求头
    print('正在识别...')
    r = requests.post(url, json=data, headers=headers)
    Result = r.json()
    if 'result' in Result:
        return Result['result'][0]
    else:
        return Result


def get_audio(file):
    """
    获取音频文件
    """
    with open(file, 'rb') as f:
        data = f.read()
    return data

def initDict(mapPath):
    d = dict()
    for line in open(mapPath, 'r'):
        if line.find('->') != -1:
            d[line.split('->')[0]] = line.split('->')[1]
    return d


def getValue(tempValue):
    value = ''
    for char in tempValue:
        if is_number(char):
            value = value + char
    return value


def is_number(s):
    try:  # 如果能运行float(s)语句，返回True（字符串s是浮点数）
        float(s)
        return True
    except ValueError:  # ValueError为Python的一种标准异常，表示"传入无效的参数"
        pass  # 如果引发了ValueError这种异常，不做任何事情（pass：不做任何事情，一般用做占位语句）
    try:
        import unicodedata  # 处理ASCii码的包
        unicodedata.numeric(s)  # 把一个表示数字的字符串转换为浮点数返回的函数
        return True
    except (TypeError, ValueError):
        pass
    return False


def getTrigger(trigger, d):
    if trigger.find("且") == -1:
        if trigger in d:
            trigger = d[trigger]
        else:
            if trigger.find('高于') != -1:
                attribute = trigger.split("高于")[0]
                tempValue = trigger.split("高于")[1]
                value = getValue(tempValue)
                trigger = d[attribute].strip() + ">" + value
            elif trigger.find('低于') != -1:
                attribute = trigger.split("低于")[0]
                tempValue = trigger.split("低于")[1]
                value = getValue(tempValue)
                trigger = d[attribute].strip() + "<" + value
            elif trigger.find('等于') != -1:
                attribute = trigger.split("等于")[0]
                tempValue = trigger.split("等于")[1]
                value = getValue(tempValue)
                trigger = d[attribute].strip() + "=" + value
    else:
        trigger1 = trigger.split('且')[0]
        trigger2 = trigger.split('且')[1]
        trigger = getTrigger(trigger1, d) + " AND " + getTrigger(trigger2,d)
    return trigger.strip()


def getTime(time):
    if time.find('秒') != -1:
        value = time.split("秒")[0]
        return value + 's'
    elif time.find('分钟') != -1:
        value = time.split("分钟")[0]
        return value + 'm'
    elif time.find('小时') != -1:
        value = time.split("小时")[0]
        return value + 'h'

def toDigit(timeValue):
    if timeValue.isdigit():
        return timeValue
    else:
        return str(int(unicodedata.numeric(timeValue)))

def getProcessID(process):
    result = os.popen("pgrep -f \'" + process + '\'');
    res = result.read()
    for line in res.splitlines():
        return line

def check_contain_chinese(check_str):
    for ch in check_str.decode('utf-8'):
        if u'\u4e00' <= ch <= u'\u9fff':
            return True
    return False

# print(c2e('办公室窗户打开和灯打开不能同时发生'))

place_list = ['卧室一', '卧室二', '卧室', '厕所一', '厕所二', '厕所', '走廊', '餐厅', '客厅', '厨房', '书房', '阳台', '户外', '屋内']
action_list = ['关灯', '开灯', '灯变亮', '灯变暗', '开热空调', '开冷空调', '空调升温', '空调降温', '关空调', '开电视', '发出警告', '开窗帘', '关窗帘', '开窗', '关窗',
               '开门', '关门', '开氛围灯', '关氛围灯', '亮一点', '暗一点', '热一点', '冷一点', '温度高一点', '温度低一点', '允许通风',
               '禁止通风', '开启回家模式', '开启离家模式', '开启观影模式']
trigger_list = ['下雨', '不下雨', '天晴', '有人', '按门铃', '没人', '有人移动', '没人移动', '着火', '起风', '起烟', '天黑', '灯打开', '灯关闭', '门打开',
                '门关闭', '窗打开', '窗关闭', '窗帘打开', '窗帘关闭', '空调制冷', '空调制热', '空调关闭', '电视打开', '电视关闭']
time_list = ['超过?小时', '超过?分钟', '超过?秒']

re_chn = re.compile("([\u4E00-\u9Fa5a-zA-Z0-9+#&]+)")


def chn_to_arabic(text: str):
    chn_numbers = re.findall(r'.*超过(.+)(秒|分钟|小时)时', text)
    if len(chn_numbers) > 0:
        arabic_number = cn2an.transform(chn_numbers[0][0], 'cn2an')
        return text.replace('超过' + ''.join(chn_numbers[0]) + '时', '超过' + str(arabic_number) + chn_numbers[0][1] + '时')
    else:
        return text


def text_extract(sent: str):
    text = ""
    blks = re_chn.split(sent)
    for blk in blks:
        if not blk:
            continue
        if re_chn.match(blk):
            text += blk
    return text


def num_extract(text: str):
    return re.sub(r'[\d]+', '', text), re.findall(r'[\d]+', text)


class Retrieval(object):
    def __init__(self):
        self.map_length_to_sent_and_pinyin = {}
        self.initialized = False

    def _initialize(self):

        for action in action_list:
            sent = action
            sent_pinyin = pypinyin.slug(sent, separator='')
            if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
            else:
                self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]

        for place in place_list:
            for action in action_list:
                sent = place + action
                sent_pinyin = pypinyin.slug(sent, separator='')
                if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                    self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                else:
                    self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
        for trigger in trigger_list:
            for action in action_list:
                sent = trigger + '时' + action
                sent_pinyin = pypinyin.slug(sent, separator='')
                if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                    self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                else:
                    self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
            for place in place_list:
                for action in action_list:
                    sent = trigger + '时' + place + action
                    sent_pinyin = pypinyin.slug(sent, separator='')
                    if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                        self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                    else:
                        self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
        for place_one in place_list:
            for trigger in trigger_list:
                for action in action_list:
                    sent = place_one + trigger + '时' + action
                    sent_pinyin = pypinyin.slug(sent, separator='')
                    if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                        self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                    else:
                        self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
                for place_two in place_list:
                    for action in action_list:
                        sent = place_one + trigger + '时' + place_two + action
                        sent_pinyin = pypinyin.slug(sent, separator='')
                        if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                            self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                        else:
                            self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
        for trigger in trigger_list:
            for time in time_list:
                for action in action_list:
                    sent = trigger + time + '时' + action
                    sent_pinyin = pypinyin.slug(sent, separator='')
                    if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                        self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                    else:
                        self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
                for place in place_list:
                    for action in action_list:
                        sent = trigger + time + '时' + place + action
                        sent_pinyin = pypinyin.slug(sent, separator='')
                        if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                            self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                        else:
                            self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
        for place_one in place_list:
            for trigger in trigger_list:
                for time in time_list:
                    for action in action_list:
                        sent = place_one + trigger + time + '时' + action
                        sent_pinyin = pypinyin.slug(sent, separator='')
                        if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                            self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                        else:
                            self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
                    for place_two in place_list:
                        for action in action_list:
                            sent = place_one + trigger + time + '时' + place_two + action
                            sent_pinyin = pypinyin.slug(sent, separator='')
                            if len(sent_pinyin) in self.map_length_to_sent_and_pinyin:
                                self.map_length_to_sent_and_pinyin[len(sent_pinyin)].append((sent_pinyin, sent))
                            else:
                                self.map_length_to_sent_and_pinyin[len(sent_pinyin)] = [(sent_pinyin, sent)]
        self.initialized = True

    def save_sent_data(self):
        self._initialize()
        with open('data/sent_data.pickle', 'wb+') as f:
            pickle.dump(self.map_length_to_sent_and_pinyin, f, pickle.DEFAULT_PROTOCOL)

    def load_sent_data(self):
        with open('data/sent_data.pickle', 'rb') as f:
            self.map_length_to_sent_and_pinyin = pickle.load(f)
        self.initialized = True

    def retrieve(self, sent):
        if not sent:
            return sent
        text = text_extract(sent)
        text = chn_to_arabic(text)
        text, nums = num_extract(text)
        number = ''
        if len(nums) > 0:
            number = nums[0]
        if not text:
            return text
        if not self.initialized:
            self.load_sent_data()
        sent_pinyin = pypinyin.slug(text, separator='')
        length = len(sent_pinyin)
        interval = 10
        top_sent = text
        min_distance = math.inf
        for cl in range(max(0, length - interval), length + interval):
            if cl in self.map_length_to_sent_and_pinyin:
                pair_list = self.map_length_to_sent_and_pinyin[cl]
                for pair in pair_list:
                    distance = Levenshtein.distance(pair[0], sent_pinyin)
                    if min_distance > distance:
                        min_distance = distance
                        top_sent = pair[1]
        res = top_sent.replace('超过?', '超过' + number)
        return res


def sound2TAP(filePath):
    TOKEN = fetch_token()  # 获取token
    speech = get_audio(FILEPATH)
    chinese = speech2text(speech, TOKEN, int(80001))
    r = retrieval.Retrieval()
    chinese_modified = r.retrieve(chinese)
    eRequirements = c2e(chinese_modified).strip()
    return eRequirements

def c2e(cRequirement):
    cemap = initDict(MAPPATH)
    rooms = ["客厅", "玄关"]
    result = []

    cRequirement = str(cRequirement)
    cRequirement = cRequirement.replace("，","")
    cRequirement = cRequirement.replace("。", "")
    cRequirement = cRequirement.replace("！", "")
    cRequirement = cRequirement.replace("？", "")
    eRequirement = ''

    room = ''
    for temp in rooms:
        if cRequirement.startswith(temp):
            room = temp
            cRequirement = cRequirement[cRequirement.index(room) + len(room):]
            break
    if room != '':
        room = cemap[room].strip()
        result.append(toTAP(cRequirement, room))
    else:
        for temp in rooms:
            room = cemap[temp].strip()
            result.append(toTAP(cRequirement, room))
    return result


def toTAP(cRequirement, room):
    cemap = initDict(MAPPATH)

    devices = []
    d = dict()
    devices = []
    for line in open(DEVICEPATH, 'r'):
        if line.startswith('device'):
            temp = line[line.find('{') + 1:line.find('}')]
            attributes = temp.split(',')
            deviceName = attributes[0].split(':')[1]
            deviceType = attributes[1].split(':')[1]
            location = attributes[2].split(':')[1]
            d[deviceType + '//' + location] = deviceName
            devices.append(deviceType)

    rooms = ["客厅", "玄关"]
    trigger = getTrigger(cRequirement.split("时")[0], cemap).strip()
    triggerRoom = room
    if trigger.split('.')[0] in devices:
        if not trigger.split('.')[0] + '//' + room in d:
            return
        trigger = d[trigger.split('.')[0] + '//' + room] + '.' + trigger.split('.')[1]

    action = cRequirement.split("时")[1].strip()
    actionRoom = ''
    actionRoomFlag = False
    for temp in rooms:
        if action.startswith(temp):
            actionRoomFlag = True
            actionRoom = cemap[temp.strip()].strip()
            action = cemap[action[action.index(temp) + len(temp):]]
            if action.split('.')[0] in devices:
                if not action.split('.')[0] + '//' + actionRoom in d:
                    return
                action = d[action.split('.')[0] + '//' + actionRoom] + '.' + action.split('.')[1]
            break
    if not actionRoomFlag:
        actionRoom = triggerRoom
        action = cemap[action]
        if action.split('.')[0] in devices:
            if not action.split('.')[0] + '//' + actionRoom in d:
                return
            action = d[action.split('.')[0] + '//' + actionRoom] + '.' + action.split('.')[1]

    return "IF " + trigger + " THEN " + action;

# print(c2e('客厅温度低于25度时开冷空调'))
#
while 1:
    interrupted = False
    # 实例化snowboy，第一个参数就是唤醒识别模型位置
    detector = snowboydecoder.HotwordDetector('xiaoxin.pmdl', sensitivity=0.5)
    print('等待唤醒')
    # snowboy监听循环
    detector.start(detected_callback=detected,
                   interrupt_check=interrupt_callback,
                   sleep_time=0.03)
    my_record()  # 唤醒成功开始录音
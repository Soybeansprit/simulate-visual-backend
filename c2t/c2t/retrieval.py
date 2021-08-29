import math
import pickle
import re

import Levenshtein
import cn2an
import pypinyin

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


if __name__ == '__main__':
    r = Retrieval()
    r.save_sent_data()

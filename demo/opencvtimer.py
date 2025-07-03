# pyright: strict

import cv2, time
from cv2.typing import MatLike

class Timer:
    def __init__(self, counter: int):
        self.counter = counter + 1
        self.display_counter = counter
        self.time_start = time.time_ns()
        self.is_done = False
        self.stopped = False
        self.time_since_should_stop = -1

    def update(self, img: MatLike, should_stop: bool) -> MatLike:
        if self.stopped:
            return img

        t = time.time_ns()

        TIME_DELTA = 1
        if should_stop:
            if self.time_since_should_stop == -1:
                self.time_since_should_stop = time.time_ns()

            if (t - self.time_since_should_stop) / 10e8 > TIME_DELTA:
                self.stop()
                return img
        else:
            self.time_since_should_stop = -1

        return self.__advance_time(t - self.time_start, img)

    def stop(self):
        self.stopped = True

    def done(self):
        self.is_done = True

    def __advance_time(self, td: int, img: MatLike) -> MatLike:
        td = int(td / 10e8)
        self.display_counter = (self.counter - 1) - td

        if self.display_counter < 0:
            self.done()
            return img

        h, w, _= img.shape
        out = img.copy()
        cv2.putText(
            out,
            str(int(self.display_counter)),
            (w // 2 - 50, h // 2),
            cv2.FONT_HERSHEY_SIMPLEX,
            10,
            (255, 255, 255) if self.time_since_should_stop < 0 else (255, 0, 0),
            50,
            cv2.LINE_AA
        )

        return out

if __name__ == '__main__':
    cap = cv2.VideoCapture(0)
    t = Timer(10)
    should_stop = False

    while not t.stopped:
        ok, frame = cap.read()
        if not ok:
            print('error!')
            break

        c = t.update(frame, should_stop)
        cv2.imshow('a', c)

        if (cv2.waitKey(1) & 0xff) == ord('s'):
            should_stop = not should_stop

        if (cv2.waitKey(1) & 0xff) == ord('q'):
            break

import cv2
import numpy as np
from opencvtimer import Timer

def detect_face(frame):
    cx, cy = (res[0] // 2, int(res[1] * 0.17))
    w, h = 110, 170
    w2, h2 = w // 2, h // 2
    rect = (
        (cx - w2, cy - h2),
        (cx + w2, cy + h2)
    )
    b = (rect[1][0] - w2, rect[1][1] + h2)

    cv2.rectangle(frame, rect[0], rect[1], (0, 0, 0), 2)

    face = classifier.detectMultiScale(
            frame,
            minSize=(40, 40),
        )

    if len(face) > 0:
        [x, y, w, h] = face[0]
        cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 3)

        change_fase = rect[0][0] <= x <= rect[1][0] and \
                      rect[0][1] <= y <= rect[1][1] and \
                      rect[0][0] <= x + w <= rect[1][0] and \
                      rect[0][1] <= y + h <= rect[1][1]


        return change_fase, frame, b

    return False, frame, None

def detect_body(frame, b):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    # suavizado (blur) - creo que no es necesario
    kernel = np.ones((3, 3)) * 5
    kernel[2][2] = 7
    kernel = kernel / np.sum(kernel)
    out = cv2.filter2D(gray, -1, kernel)

    out = cv2.Canny(out, 60, 200)

    # https://docs.opencv.org/4.x/d9/d8b/tutorial_py_contours_hierarchy.html
    contours, _ = cv2.findContours(out, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # out = cv2.cvtColor(out, cv2.COLOR_GRAY2BGR)
    cv2.circle(frame, b, 10, (255, 0, 0), -1)
    cv2.drawContours(frame, contours, -1, (0, 0, 255), 4)

    # cv2.imshow('out', out)

    cont = np.zeros((0, 1, 2), dtype=np.int32)
    for cnt in contours:
        d = cv2.pointPolygonTest(cnt, b, True)
        if abs(d) < 200:
            cont = np.vstack([cont, cnt])
            # print('found it')

    # print(cont)
    # print(contours)

    hull = cv2.convexHull(cont)

    if hull is None:
        return frame, False

    detected = False
    if cv2.pointPolygonTest(hull, b, False) > 0:
        cv2.drawContours(frame, [hull], -1, (0, 255, 0), 4)
        cv2.drawContours(frame, cont, -1, (0, 255, 255), 4)
        detected = True

    return frame, detected


if __name__ == '__main__':
    cap = cv2.VideoCapture(0)
    classifier = cv2.CascadeClassifier(
        cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    )

    res = (420, 720)
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    recorder = cv2.VideoWriter('output.mp4', fourcc, 24, res)

    fase = 0
    # b = None
    b = (res[0] // 2, res[1] // 2)
    timer = None

    while True:
        ok, frame = cap.read()
        if not ok:
            break

        frame = cv2.resize(frame, (1280, 720))[:, 430:850]

        # fase 0 - buscar la cara del usuario
        if fase == 0:
            ok, frame, b = detect_face(frame)
            if ok:
                if not timer:
                    timer = Timer(3)

            if timer:
                frame = timer.update(frame, not ok)
                if timer.is_done:
                    fase = 1
                    timer = None
                elif timer.stopped:
                    timer = None

        # fase 1: buscar que se mantenga centrado
        elif fase == 1:
            recorder.write(frame)

            frame, detected = detect_body(frame, b)
            if not detected:
                if not timer:
                    timer = Timer(1)
            else:
                if timer:
                    timer = None

            if timer:
                frame = timer.update(frame, detected)

            if timer and timer.is_done:
                break

        if (cv2.waitKey(1) & 0xff) == ord('q'):
            break

        cv2.imshow('frameeeee', frame)

    cap.release()
    recorder.release()

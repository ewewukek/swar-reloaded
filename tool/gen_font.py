# python 3.6.0
import math
import sys
from PIL import Image, ImageMode

lw = 0.5
gr = 6
fm = 3.5

im = Image.new("RGB", (256, 256))
w = im.size[0]
h = im.size[1]
pix = im.load()

res = [[0 for x in range(w)] for y in range(h)]

def put_res(x, y, d):
    x += origin_x
    y += origin_y
    if x < 0 or x >= w or y < 0 or y >= h: return
    if d < lw:
        r = 1
    else:
        d = (d - lw) / gr
        r = (1 - d) / (1 + fm * d)
    if r > res[y][x]: res[y][x] = r

def point(x0, y0):
    for y in range(int(y0) - gr - 1, int(y0) + gr + 2):
        for x in range(int(x0) - gr - 1, int(x0) + gr + 2):
            d = math.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0))
            put_res(x, y, d)

def line(x1, y1, x2, y2):
    ex = x2 - x1
    ey = y2 - y1
    l = math.sqrt(ex * ex + ey * ey)
    ex /= l
    ey /= l
    nx = -ey
    ny = ex
    for y in range(int(min(y1, y2)) - gr - 1, int(max(y1, y2)) + gr + 2):
        for x in range(int(min(x1, x2)) - gr - 1, int(max(x2, x1)) + gr + 2):
            if (x - x1) * ex + (y - y1) * ey < 0:
                continue
            elif (x - x2) * ex + (y - y2) * ey > 0:
                continue
            else:
                d = abs((x - x1) * nx + (y - y1) * ny)
            put_res(x, y, d)
    point(x1, y1)
    point(x2, y2)

def circle(cx, cy, r, a1, a2):
    a1 = math.radians(a1)
    a2 = math.radians(a2)
    e1x = math.sin(a1)
    e1y = -math.cos(a1)
    e2x = math.sin(a2)
    e2y = -math.cos(a2)
    n1x = e1y
    n1y = -e1x
    n2x = -e2y
    n2y = e2x
    whole = a1 == a2
    big_arc = e1x * n2x + e1y * n2y > 0
    for y in range(int(cy - r) - gr - 1, int(cy + r) + gr + 2):
        for x in range(int(cx - r) - gr - 1, int(cx + r) + gr + 2):
            rx = x - cx
            ry = y - cy
            l1 = rx * n1x + ry * n1y
            l2 = rx * n2x + ry * n2y
            if (not whole and ((big_arc and l1 > 0 and l2 > 0)
            or (not big_arc and (l1 > 0 or l2 > 0)))):
                continue
            d = abs(r - math.sqrt(rx * rx + ry * ry))
            put_res(x, y, d)
    if not whole:
        point(cx + e1x * r, cy + e1y * r)
        point(cx + e2x * r, cy + e2y * r)

def ellipse(cx, cy, r1, r2, a1, a2):
    if a2 < a1:
        ellipse(cx, cy, r1, r2, a1, 360)
        ellipse(cx, cy, r1, r2, 0, a2)
        return
    if a1 == a2:
        a1 = 0
        a2 = 360
    a = a1
    while a <= a2:
        x0 = cx + math.sin(math.radians(a)) * r1
        y0 = cy - math.cos(math.radians(a)) * r2
        for y in range(int(y0) - gr - 1, int(y0) + gr + 2):
            for x in range(int(x0) - gr - 1, int(x0) + gr + 2):
                d = math.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0))
                put_res(x, y, d)
        a += 0.5

fh = 30
fo = 6

origin_x = fo
origin_y = fo
prev_lw = 0

def letter(l, lw):
    global origin_x
    global origin_y
    global prev_lw
    origin_x += prev_lw
    if origin_x + lw + fo * 2 >= w:
        origin_x = fo
        origin_y += fh + fo * 2
    prev_lw = lw + fo * 2
    print("        glyphs.put('%s', new Glyph(%d, %d, %d, %d));" % (l, origin_x - fo, origin_y - fo, lw + fo * 2, fh + fo * 2))

letter('A', 20)
line(0, 30, 10, 0)
line(10, 0, 20, 30)
line(4, 20, 16, 20)

letter('B', 20)
line(0, 0, 0, 30)
line(0, 0, 12.5, 0)
line(0, 15, 12.5, 15)
line(0, 30, 12.5, 30)
circle(12.5, 7.5, 7.5, 0, 180)
circle(12.5, 22.5, 7.5, 0, 180)

letter('C', 20)
ellipse(10, 15, 10, 15, 180, 360)
ellipse(10, 7.5, 10, 7.5, 0, 85)
ellipse(10, 22.5, 10, 7.5, 95, 180)

letter('D', 20)
line(0, 0, 0, 30)
line(0, 0, 5, 0)
line(0, 30, 5, 30)
circle(5, 15, 15, 0, 180)

letter('E', 20)
line(0, 0, 0, 30)
line(0, 0, 20, 0)
line(0, 15, 15, 15)
line(0, 30, 20, 30)

letter('F', 20)
line(0, 0, 0, 30)
line(0, 0, 20, 0)
line(0, 15, 15, 15)

letter('G', 20)
ellipse(10, 15, 10, 15, 180, 360)
circle(10, 10, 10, 0, 60)
circle(10, 20, 10, 90, 180)
line(20, 17.5, 20, 20)
line(15, 17.5, 20, 17.5)

letter('H', 20)
line(0, 0, 0, 30)
line(20, 0, 20, 30)
line(0, 15, 20, 15)

letter('I', 0)
line(0, 0, 0, 30)

letter('J', 20)
line(20, 0, 20, 20)
circle(10, 20, 10, 90, 270)

letter('K', 20)
line(0, 0, 0, 30)
line(0, 15, 5, 15)
line(5, 15, 20, 0)
line(5, 15, 20, 30)

letter('L', 20)
line(0, 0, 0, 30)
line(0, 30, 20, 30)

letter('M', 20)
line(0, 0, 0, 30)
line(0, 0, 10, 15)
line(10, 15, 20, 0)
line(20, 0, 20, 30)

letter('N', 20)
line(0, 0, 0, 30)
line(0, 0, 20, 30)
line(20, 0, 20, 30)

letter('O', 20)
ellipse(10, 15, 10, 15, 0, 0)

letter('P', 20)
line(0, 0, 0, 30)
line(0, 0, 12.5, 0)
line(0, 15, 12.5, 15)
circle(12.5, 7.5, 7.5, 0, 180)

letter('Q', 20)
ellipse(10, 15, 10, 15, 0, 0)
line(12.5, 22.5, 20, 30)

letter('R', 20)
line(0, 0, 0, 30)
line(0, 0, 12.5, 0)
line(0, 15, 12.5, 15)
circle(12.5, 7.5, 7.5, 0, 180)
line(10, 15, 20, 30)

letter('S', 20)
ellipse(10, 7.5, 10, 7.5, 180, 85)
ellipse(10, 22.5, 10, 7.5, 0, 275)

letter('T', 20)
line(0, 0, 20, 0)
line(10, 0, 10, 30)

letter('U', 20)
line(0, 0, 0, 20)
line(20, 0, 20, 20)
circle(10, 20, 10, 90, 270)

letter('V', 20)
line(0, 0, 10, 30)
line(10, 30, 20, 0)

letter('W', 20)
line(0, 0, 0, 30)
line(0, 30, 10, 17.5)
line(10, 17.5, 20, 30)
line(20, 0, 20, 30)

letter('X', 20)
line(0, 0, 20, 30)
line(0, 30, 20, 0)

letter('Y', 20)
line(0, 0, 10, 15)
line(10, 15, 20, 0)
line(10, 15, 10, 30)

letter('Z', 20)
line(0, 0, 20, 0)
line(0, 30, 20, 0)
line(0, 30, 20, 30)

letter('1', 10)
line(0, 10, 10, 0)
line(10, 0, 10, 30)

letter('2', 20)
ellipse(10, 7.5, 10, 7.5, 275, 135)
line(0, 30, 17, 13)
line(0, 30, 20, 30)

letter('3', 20)
ellipse(10, 7.5, 10, 7.5, 275, 180)
ellipse(10, 22.5, 10, 7.5, 0, 265)

letter('4', 20)
line(15, 0, 15, 30)
line(0, 20, 20, 20)
line(0, 20, 15, 0)

letter('5', 20)
line(0, 0, 20, 0)
line(0, 0, 0, 12.5)
line(0, 12.5, 10, 12.5)
ellipse(10, 20, 10, 7.5, 0, 90)
circle(10, 20, 10, 90, 180)
ellipse(10, 22.5, 10, 7.5, 180, 265)

letter('6', 20)
circle(10, 10, 10, 270, 360)
ellipse(10, 7.5, 10, 7.5, 0, 85)
line(0, 10, 0, 21.25)
ellipse(10, 21.25, 10, 8.75, 0, 0)

letter('7', 20)
line(0, 0, 20, 0)
line(20, 0, 0, 30)

letter('8', 20)
ellipse(10, 6.25, 7.5, 6.25, 0, 0)
ellipse(10, 21.25, 10, 8.75, 0, 0)

letter('9', 20)
ellipse(10, 8.75, 10, 8.75, 0, 0)
line(20, 8.75, 20, 20)
circle(10, 20, 10, 90, 180)
ellipse(10, 22.5, 10, 7.5, 180, 265)

letter('0', 20)
ellipse(10, 15, 10, 15, 0, 0)
line(13, 10, 7, 20)

for y in range(0, h):
    for x in range(0, w):
        c = int(255 * res[y][x])
        pix[x, y] = (c, c, c)

im.show()
im.save(sys.argv[1], "PNG")
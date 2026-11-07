import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

os.makedirs('store_assets', exist_ok=True)

def create_gradient_radial(width, height, center_color, edge_color):
    img = Image.new("RGBA", (width, height), edge_color)
    draw = ImageDraw.Draw(img)
    cx, cy = width / 2, height / 2
    max_radius = math.hypot(cx, cy)
    for r in range(int(max_radius), 0, -2):
        factor = r / max_radius
        # Interpolate
        r_c = int(edge_color[0] * factor + center_color[0] * (1 - factor))
        g_c = int(edge_color[1] * factor + center_color[1] * (1 - factor))
        b_c = int(edge_color[2] * factor + center_color[2] * (1 - factor))
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(r_c, g_c, b_c, 255))
    return img

def generate_app_icon():
    width, height = 512, 512
    # Rich deep brand gradient
    icon = Image.new("RGBA", (width, height), (15, 12, 41, 255))
    draw = ImageDraw.Draw(icon)
    
    # Background subtle gradient
    for y in range(height):
        ratio = y / height
        r = int(19 + (80 - 19) * ratio)
        g = int(14 + (40 - 14) * ratio)
        b = int(58 + (150 - 58) * ratio)
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))
        
    # Glowing center circle
    cx, cy = 256, 256
    draw.ellipse([cx - 180, cy - 180, cx + 180, cy + 180], fill=(124, 92, 233, 90))
    draw.ellipse([cx - 150, cy - 150, cx + 150, cy + 150], fill=(99, 102, 241, 200), outline=(255, 184, 0, 220), width=6)
    
    # Graduation Cap / Shield Symbol (Polygon)
    # Diamond top of cap
    diamond = [
        (cx, cy - 80),
        (cx + 105, cy - 35),
        (cx, cy + 10),
        (cx - 105, cy - 35),
    ]
    draw.polygon(diamond, fill=(255, 255, 255, 255))
    
    # Under-cap skull base
    cap_base = [
        (cx - 65, cy - 15),
        (cx + 65, cy - 15),
        (cx + 50, cy + 50),
        (cx - 50, cy + 50),
    ]
    draw.polygon(cap_base, fill=(240, 240, 250, 255))
    
    # Golden Tassel
    draw.line([(cx, cy - 35), (cx + 115, cy - 5)], fill=(255, 184, 0, 255), width=6)
    draw.ellipse([cx + 110, cy - 7, cx + 124, cy + 22], fill=(255, 184, 0, 255))
    
    # Star of Excellence below cap
    star_points = []
    outer_r = 38
    inner_r = 16
    star_cy = cy + 95
    for i in range(10):
        angle = i * (math.pi / 5) - (math.pi / 2)
        r = outer_r if i % 2 == 0 else inner_r
        star_points.append((cx + r * math.cos(angle), star_cy + r * math.sin(angle)))
    draw.polygon(star_points, fill=(255, 184, 0, 255), outline=(255, 255, 255, 200), width=2)
    
    icon.save('store_assets/play_store_icon_512.png', 'PNG')
    print("Saved store_assets/play_store_icon_512.png (512x512)")

def generate_feature_graphic():
    width, height = 1024, 500
    feat = Image.new("RGBA", (width, height), (11, 8, 30, 255))
    draw = ImageDraw.Draw(feat)
    
    # Background gradient
    for x in range(width):
        ratio = x / width
        r = int(14 + (40 - 14) * ratio)
        g = int(10 + (20 - 10) * ratio)
        b = int(45 + (100 - 45) * ratio)
        draw.line([(x, 0), (x, height)], fill=(r, g, b, 255))
        
    # Ambient light glow circles
    draw.ellipse([700, 50, 1050, 400], fill=(124, 92, 233, 80))
    draw.ellipse([80, -50, 450, 320], fill=(99, 102, 241, 60))
    
    # Right side: Visual Adventure Quest representation
    # Stepping stones connected by glowing line
    points = [
        (680, 360),
        (760, 270),
        (850, 320),
        (920, 180),
    ]
    # Trail
    for i in range(len(points) - 1):
        draw.line([points[i], points[i+1]], fill=(255, 184, 0, 180), width=6)
        
    # Stones
    for idx, pt in enumerate(points[:-1]):
        px, py = pt
        draw.ellipse([px - 28, py - 28, px + 28, py + 28], fill=(99, 102, 241, 230), outline=(255, 255, 255, 200), width=3)
        draw.text((px - 7, py - 12), f"{idx + 1}", fill=(255, 255, 255, 255), font_size=20)
        
    # Grand Trophy finish stone
    tx, ty = points[-1]
    draw.ellipse([tx - 40, ty - 40, tx + 40, ty + 40], fill=(255, 184, 0, 255), outline=(255, 255, 255, 255), width=4)
    # Trophy star
    star_points = []
    outer_r = 24
    inner_r = 10
    for i in range(10):
        angle = i * (math.pi / 5) - (math.pi / 2)
        r = outer_r if i % 2 == 0 else inner_r
        star_points.append((tx + r * math.cos(angle), ty + r * math.sin(angle)))
    draw.polygon(star_points, fill=(255, 255, 255, 255))

    # Left side: Typography & Branding
    # Title
    draw.text((70, 90), "KidCurate", fill=(255, 255, 255, 255), font_size=56)
    draw.text((72, 160), "Parent-Controlled YouTube Learning for Kids", fill=(255, 184, 0, 255), font_size=22)
    draw.text((72, 200), "Safe, curated video roadmaps with interactive mini-quizzes.\nNo ads, no algorithmic recommendations, 100% focused.", fill=(203, 213, 225, 255), font_size=15)
    
    # Feature Badges
    badges = [
        ("Shield", "100% Parent Controlled & Ad-Free", 280),
        ("Sparkles", "AI-Curated Weekly Learning Quest", 330),
        ("Trophy", "Interactive Comprehension Quizzes", 380),
    ]
    for icon_name, text, y_pos in badges:
        # Badge Pill
        draw.rounded_rectangle([72, y_pos, 520, y_pos + 38], radius=10, fill=(30, 24, 60, 220), outline=(124, 92, 233, 140), width=1)
        draw.ellipse([84, y_pos + 9, 104, y_pos + 29], fill=(124, 92, 233, 200))
        draw.text((118, y_pos + 9), text, fill=(248, 250, 252, 255), font_size=13)
        
    feat.save('store_assets/play_store_feature_graphic_1024x500.png', 'PNG')
    print("Saved store_assets/play_store_feature_graphic_1024x500.png (1024x500)")

if __name__ == '__main__':
    generate_app_icon()
    generate_feature_graphic()


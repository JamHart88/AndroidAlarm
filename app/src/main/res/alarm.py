import os
import time
from datetime import datetime, timedelta
import pygame
import urllib.request
import json

# Set the working directory to the script's location to ensure assets are found
os.chdir(os.path.dirname(os.path.abspath(__file__)))

class AlarmClockApp:
    def __init__(self):
        pygame.init()
        self.width = 1280
        self.height = 800
        self.screen = pygame.display.set_mode((self.width, self.height))
        pygame.display.set_caption("Alarm Clock")
        
        self.font = pygame.font.SysFont(None, 32)
        # State
        now = datetime.now()
        future = now + timedelta(minutes=1)
        self.alarm_hour = future.hour
        self.alarm_minute = future.minute
        self.alarm_set_time = f"{self.alarm_hour:02d}:{self.alarm_minute:02d}"
        
        self.previewing_alarm = False
        self.is_setting_alarm = False
        self.is_setting_brightness = False
        self.showing_warning_popup = False
        self.warning_message = ""
        self.holidays_cache = {}
        self.is_selecting_sound = False
        self.sound_files = self.find_sound_files()
        self.active_alarm_file = 'alarm-digital.wav'
        self.selected_sound_file = self.active_alarm_file
        self.preview_sound_obj = None
        self.brightness_level = 1.0
        self.day_brightness = 1.0
        self.night_brightness = 0.5
        self.setting_stage = 'hours' # 'hours' or 'minutes'
        self.alarm_active = False
        self.preview_end_time = 0
        
        # Slider dragging state
        self.dragging_slider = False

        # Asset Loading
        self.assets = {}
        self.load_assets()
        self.alarm_sound = self.load_sound(self.active_alarm_file)
        
    def find_sound_files(self):
        files = []
        assets_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'assets')
        if os.path.exists(assets_dir):
            for f in os.listdir(assets_dir):
                if f.lower().endswith(('.mp3', '.wav')):
                    files.append(f)
        files.sort()
        return files
        
    def fix_path(self, path):
        """Return the case-corrected path if file exists, else original."""
        if os.path.exists(path):
            return path
            
        # Try case-insensitive lookup
        directory, filename = os.path.split(path)
        if not os.path.exists(directory):
            return path
            
        actual_files = {f.lower(): f for f in os.listdir(directory)}
        actual_name = actual_files.get(filename.lower())
        
        if actual_name:
            return os.path.join(directory, actual_name)
        return path

    def load_image(self, name):
        path = self.fix_path(name)
        try:
            img = pygame.image.load(path).convert_alpha()
            return img
        except Exception as e:
            print(f"Error loading {path}: {e}")
            return None

    def load_sound(self, filename):
        path = self.fix_path(os.path.join('assets', filename))
        try:
            return pygame.mixer.Sound(path)
        except Exception as e:
            print(f"Error loading sound: {e}")
            return None

    def load_assets(self):
        # Map characters to filenames
        chars = {
            '0': 'assets/zero.png', '1': 'assets/one.png', '2': 'assets/two.png',
            '3': 'assets/three.png', '4': 'assets/four.png', '5': 'assets/five.png',
            '6': 'assets/six.png', '7': 'assets/seven.png', '8': 'assets/eight.png',
            '9': 'assets/nine.png', ':': 'assets/colon.png', '.': 'assets/dot.png'
        }
        for k, v in chars.items():
            self.assets[k] = self.load_image(v)
            
        # Other UI elements
        ui_files = {
            'sound_settings': 'assets/settings.png',
            'set_alarm': 'assets/set_alarm.png',
            'set_alarm_inactive': 'assets/set_alarm_inactive.png',
            'brightness': 'assets/brightness.png',
            'brightness_off': 'assets/brightness_off.png',
            'slider_track': 'assets/slider.png',
            'slider_knob': 'assets/button.png',
            'alarm_on': 'assets/alarm.png',
            'alarm_off': 'assets/alarm_off.png',
            'minus': 'assets/minus.png',
            'set': 'assets/set.png',
            'plus': 'assets/plus.png',
            'am': 'assets/am.png',
            'pm': 'assets/pm.png'
        }
        for k, v in ui_files.items():
            self.assets[k] = self.load_image(v)
            
        # Months
        months = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec']
        for m in months:
            self.assets[m] = self.load_image(f'assets/{m}.png')

    def draw_image_contain(self, img_key, rect, opacity=255):
        img = self.assets.get(img_key)
        if not img:
            # Draw red placeholder for missing assets
            s = pygame.Surface((rect.width, rect.height), pygame.SRCALPHA)
            s.fill((255, 0, 0, 128))
            self.screen.blit(s, rect)
            return

        iw, ih = img.get_size()
        scale = min(rect.width / iw, rect.height / ih)
        nw, nh = int(iw * scale), int(ih * scale)
        
        scaled = pygame.transform.smoothscale(img, (nw, nh))
        if opacity < 255:
            scaled.set_alpha(opacity)
            
        x = rect.x + (rect.width - nw) // 2
        y = rect.y + (rect.height - nh) // 2
        self.screen.blit(scaled, (x, y))

    def get_rects(self):
        # Define layout areas based on original Kivy pos_hints
        return {
            'sound_settings': pygame.Rect(25, 16, 120, 100),
            'settings': pygame.Rect(166, 16, 120, 100),
            'brightness': pygame.Rect(307, 16, 120, 100),
            'date_container': pygame.Rect(1280 - 300 - 25, 16, 300, 80),
            'digits_container': pygame.Rect(128, 200, 1024, 320),
            'alarm_btn': pygame.Rect(515, 520, 250, 200),
            'controls_layout': pygame.Rect(440, 520, 400, 100),
            'brightness_container': pygame.Rect(390, 560, 500, 80),
            # Popup Rects
            'popup_bg': pygame.Rect(340, 150, 600, 500),
            'popup_select_btn': pygame.Rect(340 + 240, 150 + 430, 120, 50),
            'warning_popup_bg': pygame.Rect(340, 250, 600, 300),
            'warning_yes': pygame.Rect(340 + 100, 250 + 200, 150, 60),
            'warning_no': pygame.Rect(340 + 350, 250 + 200, 150, 60)
        }

    def run(self):
        clock = pygame.time.Clock()
        running = True
        
        while running:
            # Event Handling
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False
                elif event.type == pygame.MOUSEBUTTONDOWN:
                    self.handle_click(event.pos)
                elif event.type == pygame.MOUSEBUTTONUP:
                    self.dragging_slider = False
                elif event.type == pygame.MOUSEMOTION:
                    if self.dragging_slider:
                        self.update_brightness_drag(event.pos)

            # Update Logic
            self.update()
            
            # Drawing
            self.draw()
            
            pygame.display.flip()
            clock.tick(30)
            
        pygame.quit()

    def handle_click(self, pos):
        rects = self.get_rects()
        
        if self.showing_warning_popup:
            self.handle_warning_popup_click(pos)
            return

        if self.is_selecting_sound:
            self.handle_sound_selection_click(pos)
            return
        
        # Sound Settings Button (New)
        if rects['sound_settings'].collidepoint(pos):
            self.is_selecting_sound = True
            self.selected_sound_file = self.active_alarm_file
        
        # Settings Button
        elif rects['settings'].collidepoint(pos):
            if not self.is_setting_alarm:
                self.enter_set_mode()
        
        # Brightness Button
        elif rects['brightness'].collidepoint(pos):
            self.toggle_brightness_mode()
            
        # Alarm Toggle (only if not setting)
        elif not self.is_setting_alarm and not self.is_setting_brightness:
            if rects['alarm_btn'].collidepoint(pos):
                self.toggle_alarm()
                
        # Controls (only if setting alarm)
        elif self.is_setting_alarm:
            base = rects['controls_layout']
            # 3 buttons spaced out
            r_minus = pygame.Rect(base.x, base.y, 100, 100)
            r_set = pygame.Rect(base.x + 150, base.y, 100, 100)
            r_plus = pygame.Rect(base.x + 300, base.y, 100, 100)
            
            if r_minus.collidepoint(pos):
                self.adjust_time(-1)
            elif r_set.collidepoint(pos):
                self.advance_stage()
            elif r_plus.collidepoint(pos):
                self.adjust_time(1)
                
        # Brightness Slider (only if setting brightness)
        elif self.is_setting_brightness:
            if rects['brightness_container'].collidepoint(pos):
                self.dragging_slider = True
                self.update_brightness_drag(pos)

    def handle_sound_selection_click(self, pos):
        rects = self.get_rects()
        popup = rects['popup_bg']
        
        # Check Select Button
        if rects['popup_select_btn'].collidepoint(pos):
            if self.preview_sound_obj:
                self.preview_sound_obj.stop()
            self.active_alarm_file = self.selected_sound_file
            self.alarm_sound = self.load_sound(self.active_alarm_file)
            self.is_selecting_sound = False
            return

        # Check List Items
        list_start_y = popup.y + 70
        for i, f in enumerate(self.sound_files):
            item_rect = pygame.Rect(popup.x + 20, list_start_y + (i * 40), popup.width - 40, 35)
            if item_rect.collidepoint(pos):
                self.selected_sound_file = f
                if self.preview_sound_obj:
                    self.preview_sound_obj.stop()
                self.preview_sound_obj = self.load_sound(f)
                if self.preview_sound_obj:
                    self.preview_sound_obj.play(-1)

    def handle_warning_popup_click(self, pos):
        rects = self.get_rects()
        if rects['warning_yes'].collidepoint(pos):
            self.activate_alarm()
            self.showing_warning_popup = False
        elif rects['warning_no'].collidepoint(pos):
            # Cancel activation
            self.showing_warning_popup = False
            # Alarm remains off

    def update_brightness_drag(self, pos):
        rects = self.get_rects()
        container = rects['brightness_container']
        knob_w = 80
        track_w = container.width
        travel = track_w - knob_w
        
        rel_x = pos[0] - (container.x + knob_w/2)
        pct = rel_x / travel if travel > 0 else 0
        val = max(0.1, min(1.0, pct))
        if self.is_night_mode():
            self.night_brightness = val
        else:
            self.day_brightness = val
        self.brightness_level = val

    def toggle_brightness_mode(self):
        self.is_setting_brightness = not self.is_setting_brightness
        if self.is_setting_brightness:
            self.is_setting_alarm = False

    def enter_set_mode(self):
        self.is_setting_alarm = True
        self.is_setting_brightness = False
        self.setting_stage = 'hours'
        try:
            h, m = map(int, self.alarm_set_time.split(':'))
            self.alarm_hour = h
            self.alarm_minute = m
        except:
            self.alarm_hour = 7
            self.alarm_minute = 0

    def adjust_time(self, delta):
        if self.setting_stage == 'hours':
            self.alarm_hour = (self.alarm_hour + delta) % 24
        else:
            self.alarm_minute = (self.alarm_minute + delta) % 60

    def advance_stage(self):
        if self.setting_stage == 'hours':
            self.setting_stage = 'minutes'
        else:
            # Exit Set Mode
            self.is_setting_alarm = False
            self.alarm_set_time = f"{self.alarm_hour:02d}:{self.alarm_minute:02d}"

    def toggle_alarm(self):
        # If alarm is currently ON, turn it OFF immediately
        if self.alarm_active:
            self.alarm_active = False
            self.previewing_alarm = False
            return

        # If turning ON, check for Weekend/Holiday warnings
        now = datetime.now()
        # Construct alarm time for today to compare
        alarm_time_today = now.replace(hour=self.alarm_hour, minute=self.alarm_minute, second=0, microsecond=0)
        
        is_tomorrow = alarm_time_today <= now
        
        if is_tomorrow:
            target_date = now.date() + timedelta(days=1)
        else:
            target_date = now.date()

        # Check 1: Is the target date Saturday (5) or Sunday (6)?
        is_weekend_setup = target_date.weekday() in [5, 6]
        
        # Check 2: Is the target date a federal holiday?
        is_holiday = self.is_federal_holiday(target_date)
        
        if is_weekend_setup or is_holiday:
            reasons = []
            if is_weekend_setup: reasons.append("Weekend")
            if is_holiday: reasons.append("Holiday")
            self.warning_message = f"Alarm for {' & '.join(reasons)}. Continue?"
            self.showing_warning_popup = True
            return

        # If no warning needed, activate immediately
        self.activate_alarm()

    def activate_alarm(self):
        self.alarm_active = True
        self.previewing_alarm = True
        self.preview_end_time = time.time() + 2

    def is_federal_holiday(self, date_obj):
        year = date_obj.year
        if year not in self.holidays_cache:
            try:
                # Fetch US holidays for the year
                url = f"https://date.nager.at/api/v3/PublicHolidays/{year}/US"
                with urllib.request.urlopen(url, timeout=3) as response:
                    data = json.loads(response.read().decode())
                    self.holidays_cache[year] = {d['date'] for d in data}
            except Exception as e:
                print(f"Could not fetch holidays: {e}")
                self.holidays_cache[year] = set()
        
        return date_obj.strftime("%Y-%m-%d") in self.holidays_cache[year]

    def is_night_mode(self):
        h = datetime.now().hour
        return h >= 23 or h < 7

    def update(self):
        # Sync brightness based on time
        if self.is_night_mode():
            self.brightness_level = self.night_brightness
        else:
            self.brightness_level = self.day_brightness

        if self.previewing_alarm and time.time() > self.preview_end_time:
            if self.alarm_active:
                self.previewing_alarm = False
        
        # Check alarm trigger
        if self.alarm_active:
            now = datetime.now()
            if now.hour == self.alarm_hour and now.minute == self.alarm_minute:
                if self.alarm_sound and self.alarm_sound.get_num_channels() == 0:
                    self.alarm_sound.play(-1)
            elif self.alarm_sound:
                self.alarm_sound.stop()
        elif self.alarm_sound:
            self.alarm_sound.stop()

    def draw(self):
        self.screen.fill((0, 0, 0))
        rects = self.get_rects()
        
        # 1. Sound Settings Button (New)
        self.draw_image_contain('sound_settings', rects['sound_settings'])

        # 2. Settings Button (Alarm Time)
        icon = 'set_alarm' if self.is_setting_alarm else 'set_alarm_inactive'
        self.draw_image_contain(icon, rects['settings'])
        
        # 3. Brightness Button
        icon = 'brightness' if self.is_setting_brightness else 'brightness_off'
        self.draw_image_contain(icon, rects['brightness'])
        
        # 4. Determine Time to Show
        now = datetime.now()
        blink_on = (int(time.time() * 2) % 2) == 0
        
        if self.is_setting_alarm:
            dt_temp = datetime(2000, 1, 1, self.alarm_hour, self.alarm_minute)
            time_str = dt_temp.strftime("%I:%M")
            ampm_str = dt_temp.strftime("%p").lower()
            date_month = now.strftime("%b").lower()
            date_day = now.strftime("%d")
            
            h_alpha = 255 if (self.setting_stage != 'hours' or blink_on) else 76
            m_alpha = 255 if (self.setting_stage != 'minutes' or blink_on) else 76
            colon_alpha = 255
            
        elif self.previewing_alarm:
            try:
                h, m = map(int, self.alarm_set_time.split(':'))
                dt_temp = datetime(2000, 1, 1, h, m)
                time_str = dt_temp.strftime("%I:%M")
                ampm_str = dt_temp.strftime("%p").lower()
            except:
                time_str = self.alarm_set_time
                ampm_str = "am"
            date_month = now.strftime("%b").lower()
            date_day = now.strftime("%d")
            h_alpha = m_alpha = colon_alpha = 255
            
        else:
            time_str = now.strftime("%I:%M")
            ampm_str = now.strftime("%p").lower()
            date_month = now.strftime("%b").lower()
            date_day = now.strftime("%d")
            h_alpha = m_alpha = 255
            colon_alpha = 255 if (time.time() % 1) < 0.5 else 0

        # 5. Draw Date
        dc = rects['date_container']
        unit_w = dc.width / 3.8
        r_month = pygame.Rect(dc.x, dc.y, int(unit_w * 2), dc.height)
        r_d0 = pygame.Rect(dc.x + int(unit_w * 2), dc.y, int(unit_w * 0.9), dc.height)
        r_d1 = pygame.Rect(dc.x + int(unit_w * 2.9), dc.y, int(unit_w * 0.9), dc.height)
        
        self.draw_image_contain(date_month, r_month)
        self.draw_image_contain(date_day[0], r_d0)
        self.draw_image_contain(date_day[1], r_d1)
        
        # 6. Draw Clock Digits
        dig_c = rects['digits_container']
        unit_w = dig_c.width / 5.3
        x_cursor = dig_c.x
        
        def draw_digit(char, width_weight, alpha):
            nonlocal x_cursor
            w = int(unit_w * width_weight)
            r = pygame.Rect(x_cursor, dig_c.y, w, dig_c.height)
            self.draw_image_contain(char, r, alpha)
            x_cursor += w
            
        draw_digit(time_str[0], 1, h_alpha)
        draw_digit(time_str[1], 1, h_alpha)
        draw_digit(':', 0.3, colon_alpha)
        draw_digit(time_str[3], 1, m_alpha)
        draw_digit(time_str[4], 1, m_alpha)
        
        r_ampm = pygame.Rect(x_cursor, dig_c.y + dig_c.height//4, int(unit_w), dig_c.height//2)
        self.draw_image_contain(ampm_str, r_ampm)

        # 7. Bottom Controls
        if self.is_setting_alarm:
            base = rects['controls_layout']
            self.draw_image_contain('minus', pygame.Rect(base.x, base.y, 100, 100))
            self.draw_image_contain('set', pygame.Rect(base.x + 150, base.y, 100, 100))
            self.draw_image_contain('plus', pygame.Rect(base.x + 300, base.y, 100, 100))
            
        elif self.is_setting_brightness:
            bc = rects['brightness_container']
            # Stretch slider track to fill container so button aligns with ends
            track_img = self.assets.get('slider_track')
            if track_img:
                scaled = pygame.transform.smoothscale(track_img, (bc.width, bc.height))
                self.screen.blit(scaled, bc)
            else:
                self.draw_image_contain('slider_track', bc)
            knob_w = 80
            travel = bc.width - knob_w
            knob_x = bc.x + (travel * self.brightness_level)
            r_knob = pygame.Rect(knob_x, bc.y, knob_w, 80)
            self.draw_image_contain('slider_knob', r_knob)
            
        else:
            icon = 'alarm_on' if self.alarm_active else 'alarm_off'
            self.draw_image_contain(icon, rects['alarm_btn'])

        # 8. Brightness Overlay
        if self.brightness_level < 1.0:
            overlay = pygame.Surface((self.width, self.height))
            overlay.fill((0, 0, 0))
            alpha = int((1.0 - self.brightness_level) * 255)
            overlay.set_alpha(alpha)
            self.screen.blit(overlay, (0, 0))

        # 9. Sound Selection Popup
        if self.is_selecting_sound:
            self.draw_sound_selection_popup()

        if self.showing_warning_popup:
            self.draw_warning_popup()

    def draw_sound_selection_popup(self):
        rects = self.get_rects()
        popup = rects['popup_bg']
        
        # Overlay
        s = pygame.Surface((self.width, self.height), pygame.SRCALPHA)
        s.fill((0, 0, 0, 200))
        self.screen.blit(s, (0, 0))
        
        # Popup Box
        pygame.draw.rect(self.screen, (40, 40, 40), popup)
        pygame.draw.rect(self.screen, (200, 200, 200), popup, 2)
        
        # Title
        title = self.font.render("Select Alarm Sound", True, (255, 255, 255))
        self.screen.blit(title, (popup.centerx - title.get_width() // 2, popup.y + 20))
        
        # List
        y = popup.y + 70
        for f in self.sound_files:
            color = (255, 255, 0) if f == self.selected_sound_file else (200, 200, 200)
            txt = self.font.render(f, True, color)
            self.screen.blit(txt, (popup.x + 30, y))
            y += 40
            
        # Select Button
        btn = rects['popup_select_btn']
        pygame.draw.rect(self.screen, (0, 100, 0), btn)
        pygame.draw.rect(self.screen, (255, 255, 255), btn, 2)
        btn_txt = self.font.render("Select", True, (255, 255, 255))
        self.screen.blit(btn_txt, (btn.centerx - btn_txt.get_width() // 2, btn.centery - btn_txt.get_height() // 2))

    def draw_warning_popup(self):
        rects = self.get_rects()
        popup = rects['warning_popup_bg']
        
        # Overlay
        s = pygame.Surface((self.width, self.height), pygame.SRCALPHA)
        s.fill((0, 0, 0, 200))
        self.screen.blit(s, (0, 0))
        
        # Popup Box
        pygame.draw.rect(self.screen, (100, 0, 0), popup)
        pygame.draw.rect(self.screen, (255, 255, 255), popup, 2)
        
        # Message
        msg = self.font.render(self.warning_message, True, (255, 255, 255))
        self.screen.blit(msg, (popup.centerx - msg.get_width() // 2, popup.y + 80))
        
        # Yes Button
        btn_yes = rects['warning_yes']
        pygame.draw.rect(self.screen, (0, 100, 0), btn_yes)
        pygame.draw.rect(self.screen, (255, 255, 255), btn_yes, 2)
        txt_yes = self.font.render("Yes", True, (255, 255, 255))
        self.screen.blit(txt_yes, (btn_yes.centerx - txt_yes.get_width() // 2, btn_yes.centery - txt_yes.get_height() // 2))

        # No Button
        btn_no = rects['warning_no']
        pygame.draw.rect(self.screen, (100, 0, 0), btn_no)
        pygame.draw.rect(self.screen, (255, 255, 255), btn_no, 2)
        txt_no = self.font.render("No", True, (255, 255, 255))
        self.screen.blit(txt_no, (btn_no.centerx - txt_no.get_width() // 2, btn_no.centery - txt_no.get_height() // 2))

if __name__ == '__main__':
    app = AlarmClockApp()
    app.run()

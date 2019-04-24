(function () {

  var root = this;
  var template = '<div class="krypton krypton_light krypton_float krypton_jigsaw"><div class="krypton_panel" id="krypton_panel">'
      + '<div class="krypton_panel-placeholder"><div class="krypton_bgimg">'
      + '<img class="krypton_bg-img" id="krypton_bg" />'
      + '<img class="krypton_jigsaw" id="krypton_jigsaw"/></div>'
      + '<div class="krypton_loadbox">'
      + '<div class="krypton_loadbox_inner"><div class="krypton_loadicon"></div>'
      + '<span class="krypton_loadtext">加载中...</span></div></div>'
      + '<div class="krypton_refresh" id="krypton_refresh" title="刷新"></div></div></div>'
      + '<div class="krypton_control"><div class="krypton_slide_indicator" id="krypton_slide_indicator"></div>'
      + '<div class="krypton_slider" id="krypton_slider"><span class="krypton_slider_icon"></span></div>'
      + '<div class="krypton_tips"><span class="krypton_tips_icon"></span>'
      + '<span class="krypton_tips_text">向右拖动滑块填充拼图</span></div></div></div>';

  var _token = "${token}";
  var _key = "${key}";
  var _validateVal = {};
  var _offset = 0, _startTamp = 0, _startX = 0;

  function ajax(url, method, data, callback) {
    // 低版本浏览器没有考虑
    var xhr = new XMLHttpRequest();
    xhr.open(method, url);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function () {
      if (xhr.readyState == 4 && xhr.status == 200 && callback) {
        callback(JSON.parse(xhr.responseText));
      }
    };
    var param = "";
    if (data) {
      var arr = [];
      for (var k in data) {
        arr.push(k + "=" + data[k]);
      }
      param = arr.join("&");
    }
    xhr.send(param);
  }

  function encrypt(raw) {
    var value = '';
    while (raw > 0) {
      value += _key[raw % 10];
      raw = ~~(raw / 10);
    }
    return value;
  }

  var Krypton = function (id, conf) {
    var _conf = conf || {};
    var _this = this;
    this.url = _conf.url || '/sliding/create';
    this.validateUrl = _conf.validateUrl || '/sliding/validate';

    this.fetch = _conf.fetch || function () {
      ajax(this.url, 'post', {
        token: _token,
        _t: new Date().getTime()
      }, function (resp) {
        krypton_bg.setAttribute("src", resp.data.background);
        krypton_jigsaw.setAttribute("src", resp.data.jigsaw);
        _this.token = resp.data.token;
      });
    };

    this.init = function () {
      document.getElementById(id).innerHTML = template;
      this.event();
      this.fetch();
    };

    this.event = function () {
      krypton_refresh.onclick = function () {
        _this.fetch();
        krypton_slider.style.left = "0px";
        krypton_jigsaw.style.left = "0px";
        krypton_slide_indicator.style.borderColor = 'transparent';
        krypton_slide_indicator.style.backgroundColor = 'unset';
        krypton_slide_indicator.style.width = '0';
      };
      krypton_slider.onmousedown = function (e) {
        e.preventDefault();
        _startX = e.clientX;
        _startTamp = new Date().getTime();
        var target = e.target;
        document.addEventListener('mousemove', mousemove);
        document.addEventListener('mouseup', mouseup);

        krypton_slide_indicator.style.borderColor = '#1991fa';
        krypton_slide_indicator.style.backgroundColor = '#d1e9fe';
        krypton_panel.style.display = "block";

        function mousemove(event) {
          _offset = Math.min(Math.max(0, event.clientX - _startX), 286);
          krypton_slider.style.left = _offset + "px";
          krypton_jigsaw.style.left = (_offset - _offset / 30) + "px";
          krypton_slide_indicator.style.width = _offset + "px";
          krypton_slider.style.transition = "none";
          krypton_jigsaw.style.transition = "none";
        }

        // 鼠标放开
        function mouseup() {
          document.removeEventListener('mousemove', mousemove);
          document.removeEventListener('mouseup', mouseup);
          _this.validate();
        }
      };
    };

    this.validate = function () {
      ajax(_this.validateUrl, 'post', {
        token: this.token,
        data: encrypt(_offset) + "/" + encrypt(
            new Date().getTime() - _startTamp)
      }, function (resp) {
        if (resp.success) {
          krypton_slide_indicator.style.borderColor = '#52ccba';
          krypton_slide_indicator.style.backgroundColor = '#d2f4ef';
          krypton_panel.style.display = "none";
          _validateVal.validate = resp.data;
          _validateVal.token = _this.token;
        } else {
          krypton_slide_indicator.style.borderColor = '#f57a7a';
          krypton_slide_indicator.style.backgroundColor = '#fce1e1';
          _validateVal = {};
        }
        _validateVal.success = resp.success;
        setTimeout(function () {
          krypton_refresh.click();
        }, 500);
      });
    };

    this.validateVal = function () {
      return _validateVal;
    };

    this.init();
  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = Krypton;
    }
    exports.Krypton = Krypton;
  } else if (typeof define === 'function' && define.amd) {
    define('Krypton', function () {
      return Krypton;
    });
  } else {
    root['Krypton'] = Krypton;
  }

}).call(this);
import {
Smartphone,
Globe,
Palette,
WifiOff,
Maximize2,
Delete,
} from "lucide-react";
import appIcon from 'figma:asset/d2b84eb33c33fad845a17b29f0f051216ef87e46.png';

export default function App() {
return (
<div
  className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-8">
  <div className="relative" style={{ width: "1024px" , height: "500px" }}>
    {/* Feature Graphic Container */}
    <div className="w-full h-full rounded-lg overflow-hidden shadow-2xl relative" style={{
      background: "linear-gradient(to top right, #5b36ac, #ac429e)" , }}>
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute inset-0" style={{
          backgroundImage: "repeating-linear-gradient(45deg, transparent, transparent 35px, rgba(255,255,255,.1) 35px, rgba(255,255,255,.1) 70px)"
          , }}></div>
      </div>

      {/* Content */}
      <div className="relative h-full flex items-center justify-between px-16">
        {/* Left Side - Branding & Main Message */}
        <div className="flex-1 z-10">
          <div className="flex items-center gap-3 mb-6">
            <img src={appIcon} alt="RatePad App Icon" className="w-14 h-14 rounded-2xl shadow-lg" />
            <div>
              <h1 className="text-white" style={{ fontSize: "44px" , lineHeight: "1.1" , }}>
                RatePad
              </h1>
              <p className="text-blue-100" style={{ fontSize: "18px" }}>
                Currency Converter Widget
              </p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 mt-6 max-w-md">
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 border border-white/20">
              <Globe className="w-5 h-5 text-white mb-1" />
              <div className="text-white" style={{ fontSize: "16px" }}>
                32 Currencies
              </div>
            </div>
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 border border-white/20">
              <Maximize2 className="w-5 h-5 text-white mb-1" />
              <div className="text-white" style={{ fontSize: "16px" }}>
                Resizable
              </div>
            </div>
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 border border-white/20">
              <Palette className="w-5 h-5 text-white mb-1" />
              <div className="text-white" style={{ fontSize: "16px" }}>
                6 Themes
              </div>
            </div>
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 border border-white/20">
              <WifiOff className="w-5 h-5 text-white mb-1" />
              <div className="text-white" style={{ fontSize: "16px" }}>
                Offline Support
              </div>
            </div>
          </div>
        </div>

        {/* Right Side - Widget Preview */}
        <div className="flex-1 flex items-end justify-center z-10 pb-0">
          <div className="relative" style={{ marginBottom: '-100px' }}>
            {/* Phone Mockup */}
            <div className="bg-slate-900 rounded-[32px] p-2.5 shadow-2xl">
              <div
                className="bg-gradient-to-br from-slate-800 to-slate-900 rounded-[26px] overflow-hidden"
                style={{ width: "240px" , height: "450px" }}>
                {/* Widget on Home Screen */}
                <div className="p-4 h-full flex flex-col">
                  {/* Time */}
                  <div className="text-white text-center mb-6" style={{ fontSize: "40px" }}>
                    12:30
                  </div>

                  {/* Currency Widget */}
                  <div
                    className="bg-gradient-to-br from-amber-50 to-orange-50 rounded-2xl shadow-xl p-3 mb-3">
                    {/* Header with currencies and values */}
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <div className="text-orange-600" style={{ fontSize: "10px" }}>
                          USD
                        </div>
                        <div className="text-orange-600" style={{ fontSize: "22px" , lineHeight: "1"
                          , }}>
                          10
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-orange-600" style={{ fontSize: "10px" }}>
                          EUR
                        </div>
                        <div className="text-orange-600" style={{ fontSize: "22px" , lineHeight: "1"
                          , }}>
                          8.59
                        </div>
                      </div>
                    </div>

                    {/* Rate update banner */}
                    <div
                      className="bg-amber-200/50 rounded-md text-center mb-1 p-1 flex items-center justify-center">
                      <span className="text-orange-600" style={{ fontSize: "8px" }}>
                        Rate updated: just now
                      </span>
                    </div>

                    {/* Number pad */}
                    <div className="grid grid-cols-3 gap-1">
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>1</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>2</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>3</button>

                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>4</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>5</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>6</button>

                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>7</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>8</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>9</button>

                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>0</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>.</button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center"
                        style={{ fontSize: "14px" }}>000</button>
                    </div>

                    {/* Last row - Backspace and Clear */}
                    <div className="flex gap-1 mt-1">
                      <button
                        className="bg-amber-100 rounded-md py-1.5 flex items-center justify-center text-orange-600 flex-1">
                        <Delete className="w-4 h-4" />
                      </button>
                      <button
                        className="bg-amber-100 rounded-md py-1.5 text-orange-600 flex items-center justify-center flex-1"
                        style={{ fontSize: "14px" }}>
                        C
                      </button>
                    </div>
                  </div>

                  {/* App Icons */}
                  <div className="flex gap-3 justify-center mt-auto">
                    <div className="w-10 h-10 bg-white/10 backdrop-blur-sm rounded-xl"></div>
                    <div className="w-10 h-10 bg-white/10 backdrop-blur-sm rounded-xl"></div>
                    <div className="w-10 h-10 bg-white/10 backdrop-blur-sm rounded-xl"></div>
                    <div className="w-10 h-10 bg-white/10 backdrop-blur-sm rounded-xl"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Decorative Elements */}
      <div className="absolute top-10 right-20 w-24 h-24 bg-white/5 rounded-full blur-3xl"></div>
      <div className="absolute bottom-10 left-20 w-32 h-32 bg-white/5 rounded-full blur-3xl"></div>
    </div>
  </div>

  {/* Download Button */}
  <div className="absolute bottom-4 text-center">
    <p className="text-slate-400" style={{ fontSize: "14px" }}>
      Feature graphic: 1024×500px
    </p>
  </div>
</div>
);
}
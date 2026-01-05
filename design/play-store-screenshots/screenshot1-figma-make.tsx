import { Globe, Palette, WifiOff, Maximize2, Delete, ArrowLeftRight } from "lucide-react";
import appIcon from "figma:asset/d2b84eb33c33fad845a17b29f0f051216ef87e46.png";

export default function Screenshot1() {
  return (
    <div
      className='flex items-center justify-center'
      style={{
        width: "1080px",
        height: "1920px",
        background: "linear-gradient(to bottom right, #5b36ac, #ac429e)",
        position: "relative",
      }}
    >
      {/* Background pattern */}
      <div className='absolute inset-0 opacity-10'>
        <div
          className='absolute inset-0'
          style={{
            backgroundImage:
              "repeating-linear-gradient(45deg, transparent, transparent 35px, rgba(255,255,255,.1) 35px, rgba(255,255,255,.1) 70px)",
          }}
        />
      </div>

      {/* Content Container */}
      <div className='relative z-10 flex flex-col items-center text-center px-10 mt-20'>
        {/* App Icon + Title */}
        <img src={appIcon} alt='RatePad Icon' className='w-40 h-40 rounded-3xl shadow-2xl mb-8' />

        <h1 className='text-white font-bold' style={{ fontSize: "80px", lineHeight: "1.1" }}>
          RatePad
        </h1>
        <p className='text-blue-100 mb-16' style={{ fontSize: "36px" }}>
          Currency Converter Widget
        </p>

        {/* Phone Mockup */}
        <div className='mt-6'>
          <div className='bg-slate-900 rounded-[48px] p-4 shadow-2xl'>
            <div
              className='bg-gradient-to-br from-slate-800 to-slate-900 rounded-[38px] overflow-hidden'
              style={{
                width: "480px",
                height: "1020px",
              }}
            >
              {/* Phone Content */}
              <div className='p-6 h-full flex flex-col'>
                {/* Time */}
                <div className='text-white text-center mb-8' style={{ fontSize: "80px" }}>
                  12:30
                </div>

                {/* Currency Widget */}
                <div className='bg-gradient-to-br from-amber-50 to-orange-50 rounded-3xl shadow-xl p-6 mb-6'>
                  {/* Header */}
                  <div className='bg-amber-100 rounded-xl p-4 mb-4'>
                    <div className='flex items-start justify-between'>
                      <div>
                        <div className='text-orange-600' style={{ fontSize: "18px" }}>
                          USD
                        </div>
                        <div className='text-orange-600' style={{ fontSize: "40px", lineHeight: "1" }}>
                          10
                        </div>
                      </div>

                      <ArrowLeftRight className='text-orange-600 w-6 h-6 mt-1' />

                      <div className='text-right'>
                        <div className='text-orange-500' style={{ fontSize: "18px" }}>
                          EUR
                        </div>
                        <div className='text-orange-500' style={{ fontSize: "40px", lineHeight: "1" }}>
                          8.59
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Update Banner */}
                  <div className='text-center py-1 mb-4'>
                    <span className='text-orange-600' style={{ fontSize: "14px" }}>
                      Rate updated: just now
                    </span>
                  </div>

                  {/* Number Pad */}
                  <div className='grid grid-cols-3 gap-2 mb-2'>
                    {["1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "000"].map((n) => (
                      <button
                        key={n}
                        className='bg-amber-100 rounded-xl flex items-center justify-center text-orange-600 hover:bg-amber-200 transition-colors'
                        style={{
                          height: "70px",
                          fontSize: "32px",
                          fontWeight: "600",
                        }}
                      >
                        {n}
                      </button>
                    ))}
                  </div>

                  {/* Bottom row */}
                  <div className='flex gap-2'>
                    <button className='bg-amber-100 rounded-xl flex items-center justify-center flex-1 text-orange-600'>
                      <Delete className='w-8 h-8' />
                    </button>
                    <button
                      className='bg-amber-100 rounded-xl flex items-center justify-center flex-1 text-orange-600'
                      style={{ fontSize: "32px" }}
                    >
                      C
                    </button>
                  </div>
                </div>

                {/* App Icons */}
                <div className='flex gap-5 justify-center mt-auto mb-5'>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                </div>

                {/* Second row of app icons */}
                <div className='flex gap-5 justify-center'>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                  <div className='w-20 h-20 bg-white/10 backdrop-blur-sm rounded-2xl'></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

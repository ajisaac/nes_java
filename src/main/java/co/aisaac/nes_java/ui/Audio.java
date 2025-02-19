package co.aisaac.nes_java.ui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Audio {
//    public portaudio.Stream stream;
    public double sampleRate;
    public int outputChannels;
    public BlockingQueue<Float> channel;

    public Audio() {
        this.channel = new ArrayBlockingQueue<Float>(44100);
    }

    public static Audio NewAudio() {
        Audio a = new Audio();
        return a;
    }

    public void Start() throws Exception {
//        portaudio.HostApi host = PortAudio.DefaultHostApi();
//        portaudio.Parameters parameters = PortAudio.HighLatencyParameters(null, host.defaultOutputDevice);
//        portaudio.Stream stream = PortAudio.OpenStream(parameters, new portaudio.PortAudioCallback() {
//            public void Callback(float[] out) {
//                Audio.this.Callback(out);
//            }
//        });
//        stream.Start();
//        this.stream = stream;
//        this.sampleRate = parameters.SampleRate;
//        this.outputChannels = parameters.Output.Channels;
    }

    public void Stop() throws Exception {
//        this.stream.Close();
    }

    public void Callback(float[] out) {
        float output = 0f;
        for (int i = 0; i < out.length; i++) {
            if (i % this.outputChannels == 0) {
                Float sample = this.channel.poll();
                if (sample != null) {
                    output = sample;
                } else {
                    output = 0f;
                }
            }
            out[i] = output;
        }
    }
}

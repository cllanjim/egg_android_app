package com.antelope.sdk.codec;

import java.nio.ByteBuffer;

import com.antelope.sdk.ACResult;
import com.antelope.sdk.capturer.ACAudioFrame;

public class ACAACSoftDecoder implements ACAudioDecoder {
	private long mDecoder = 0;
	private byte[] mESdata = null;
	private ACStreamPacket mStreamPacket = new ACStreamPacket();

	@Override
	public ACResult initialize(int sampleRate, int channelCount) {
		if (mDecoder != 0) {
			return ACResult.SUCCESS;
		}
		long decoder = ACAudioSoftDecoder.Create(ACCodecID.AC_CODEC_ID_AAC);
		if (decoder == 0) {
			return new ACResult(ACResult.ACS_UNKNOWN, "failed to create native aac decoder");
		}
		int result = ACAudioSoftDecoder.Initialize(decoder, sampleRate, channelCount);
		if (result != ACResult.ACS_OK) {
			return new ACResult(result, "failed to initialize native aac decoder");
		}
		mDecoder = decoder;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult release() {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		ACAudioSoftDecoder.Release(mDecoder);
		mDecoder = 0;
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult decode(ACStreamPacket packet, ACAudioFrame frame) {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		if (packet.type == ACFrameType.AC_AAC_TYPE_INFO) {
			if (mESdata == null) {
				mESdata = new byte[2];
			}
			mESdata[0] = packet.buffer.get(packet.offset);
			mESdata[1] = packet.buffer.get(packet.offset + 1);
			return ACResult.IN_PROCESS;
		} else if (mESdata == null) {
			return ACResult.IN_PROCESS;
		} else {
			if (mStreamPacket.buffer == null || mStreamPacket.buffer.capacity() < (packet.size + 7)) {
				mStreamPacket.buffer = ByteBuffer.allocateDirect(packet.size + 7);
			} else {
				mStreamPacket.buffer.clear();
			}
		}
		translateAudioConfig(mStreamPacket.buffer, packet.size);
		packet.buffer.limit(packet.offset + packet.size);
		packet.buffer.position(packet.offset);
		mStreamPacket.buffer.put(packet.buffer);
		mStreamPacket.offset = 0;
		mStreamPacket.size = packet.size + 7;
		mStreamPacket.timestamp = packet.timestamp;
		mStreamPacket.type = packet.type;
		int result = ACAudioSoftDecoder.Decode(mDecoder, mStreamPacket, frame);
		if (result != ACResult.ACS_OK) {
			if (result == ACResult.ACC_AAC_DECODE_GOTFRAME_FAILED) {
				return ACResult.IN_PROCESS;
			}
			return new ACResult(result, null);
		}
		return ACResult.SUCCESS;
	}

	@Override
	public ACResult reset() {
		if (mDecoder == 0) {
			return ACResult.UNINITIALIZED;
		}
		ACAudioSoftDecoder.Reset(mDecoder);
		return ACResult.SUCCESS;
	}

	private void translateAudioConfig(ByteBuffer buffer, int dataSize) {
		byte profile = (byte) ((mESdata[0] & 0xf8) >> 3); // 取前5bit
		byte freqIdx = (byte) (((mESdata[0] & 0x07) << 1) | (mESdata[1] >> 7));// 取后4bit
		byte chanCfg = (byte) ((mESdata[1] >> 3) & 0x0f); // 取后4bit
		byte frameLengthFlag = (byte) ((mESdata[1] >> 2) & 0x01); // 取后1bit
		byte dependsOnCoreCoder = (byte) ((mESdata[1] >> 1) & 0x01); // 取后1bit
		byte extensionFlag = (byte) (mESdata[1] & 0x01); // 最后1bit
		byte num_data_block = 0; // Number of AAC frames (RDBs) in ADTS frame
									// minus 1, for maximum compatibility always
									// use 1 AAC frame per ADTS frame
		int aaclenth = dataSize + 7;
		byte caos;

		/* Sync point over a full byte */
		buffer.put((byte) 0xff);
		/*
		 * Sync point continued over first 4 bits + static 4 bits (ID, layer,
		 * protection)
		 */
		buffer.put((byte) 0xf1);
		/* Object type over first 2 bits */
		caos = (byte) (((profile - 1) << 6) & 0xc0);//
		/* rate index over next 4 bits */
		caos |= (byte) ((freqIdx << 2) & 0x3c);
		/* channels over last 2 bits */
		caos |= (byte) (((chanCfg & 0x4) >> 2) & 0x01);
		buffer.put(caos);
		/* channels continued over next 2 bits + 4 bits at zero */
		caos = (byte) (((chanCfg & 0x3) << 6) & 0xc0);
		/* frame size over last 2 bits */
		caos |= (byte) (((aaclenth & 0x1800) >> 11) & 0x03);// the upper 2 bit
		buffer.put(caos);
		/* frame size continued over full byte */
		buffer.put((byte) (((aaclenth & 0x1FF8) >> 3) & 0xff));// the middle 8
																// bit
		/* frame size continued first 3 bits */
		caos = (byte) (((aaclenth & 0x7) << 5) & 0xe0);
		/* buffer fullness (0x7FF for VBR) over 5 last bits */
		caos |= (byte) 0x1f;
		buffer.put(caos);
		/*
		 * buffer fullness (0x7FF for VBR) continued over 6 first bits + 2 zeros
		 * number of raw data blocks
		 */
		caos = (byte) 0xfc;// one raw data blocks .
		caos |= (byte) (num_data_block & 0x03); // Set raw Data blocks.
		buffer.put(caos);
	}

}

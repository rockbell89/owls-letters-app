import LetterPicture from '../LetterPicture/LetterPicture';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Scrollbar } from 'swiper';

import styles from './LetterPictureWrapper.module.scss';

type LetterPictureWrapperProps = {
  pictures: string[] | null;
  onClick?: (idx: number) => void;
  onRemove?: (idx: number) => void;
  onAdd?: (event: React.ChangeEvent<HTMLInputElement>) => void;
  isRead: boolean;
};

const LetterPictureWrapper = ({
  pictures,
  onClick,
  isRead,
  onAdd,
  onRemove,
}: LetterPictureWrapperProps) => {
  return (
    <div className={styles.pictures}>
      <Swiper
        modules={[Scrollbar]}
        spaceBetween={16}
        slidesPerView={'auto'}
        scrollbar={{ draggable: true }}
        className={styles.swiper}
      >
        {pictures &&
          pictures.length > 0 &&
          pictures.map((picture, idx) => (
            <SwiperSlide key={picture} className={styles.swiper_slide}>
              <LetterPicture
                pic={picture}
                rotate={idx}
                onClick={
                  isRead ? onClick?.bind(null, idx) : onRemove?.bind(null, idx)
                }
              />
            </SwiperSlide>
          ))}
        {/* 글쓰기 모드 */}
        {!isRead && (
          <SwiperSlide className={styles.swiper_slide}>
            <LetterPicture pic={''} isAdd rotate={1} onAdd={onAdd} />
          </SwiperSlide>
        )}
      </Swiper>
    </div>
  );
};

export default LetterPictureWrapper;

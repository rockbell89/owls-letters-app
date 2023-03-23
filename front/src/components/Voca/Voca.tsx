import { useRef, useState } from 'react';
import { AiOutlinePlus } from 'react-icons/ai';
import { useRecoilValue, useSetRecoilState } from 'recoil';
import { VocaDataType } from '../../utils/types/voca';
import VocaCard from './VocaCard/VocaCard';
import Button from '../Common/Button/Button';
import VocaModal from './VocaModal/VocaModal';
import { DELETE, GET } from '../../utils/axios';
import { deleteVocaState, selectedVocaState } from '../../recoil/atoms/voca';
import AlertModal from '../Common/Modal/AlertModal';
import useInfiniteScroll from '../../hooks/useInfiniteScroll';
import styles from './Voca.module.scss';
import Empty from '../Common/Empty/Empty';
import { TbVocabulary } from 'react-icons/tb';

const Voca = () => {
  const setSelectedVoca = useSetRecoilState(selectedVocaState);
  const deleteVocaId = useRecoilValue(deleteVocaState);
  const [isOpenModal, setIsOpenModal] = useState<boolean>(false);
  const [isEditMode, setIsEditMode] = useState<boolean>(false);
  const [isAlertOpen, setIsAlertOpen] = useState<boolean>(false);
  const [vocaList, setVocaList] = useState<VocaDataType[]>([]);
  const pageRef = useRef<number>(0);
  const isStopRef = useRef<boolean>(false);

  /**
   * @description Vocabs API
   */
  const getVocaList = async (page: number) => {
    if (isStopRef.current) return;

    try {
      const { data } = await GET(`/vocabs?page=${page}&size=10`);
      isStopRef.current = data.last;
      const formattedVacaList = data.content.map((voca: VocaDataType) => ({
        vocabId: voca.vocabId,
        meaning: voca.meaning,
        word: voca.word,
        nation: voca.nation,
      }));

      setVocaList((prev) => [...prev, ...formattedVacaList]);
    } catch (error) {
      console.log(error);
    }
  };

  const sentinelRef = useInfiniteScroll(async (page: number) => {
    await getVocaList(page);
    pageRef.current++;
  }, pageRef.current);

  /**
   * @description Vocabs 삭제 API
   */
  const deleteVoca = async () => {
    if (!deleteVocaId) return;
    try {
      await DELETE(`vocabs/${deleteVocaId}`);
      const newVocaList = vocaList.filter(
        (voca: VocaDataType) => voca.vocabId !== deleteVocaId
      );
      setVocaList(newVocaList);

      setIsAlertOpen(false);
    } catch (error) {
      // TODO: 에러 처리
      console.log(error);
    }
  };

  const onAddModalHandler = () => {
    setIsOpenModal(true);
    setIsEditMode(false);
  };

  const onEditModalHandler = (voca: VocaDataType) => {
    setSelectedVoca(voca);
    setIsOpenModal(true);
    setIsEditMode(true);
  };

  const onDeleteHandler = () => {
    setIsAlertOpen(true);
  };

  const setNewVocaList = (newVoca: VocaDataType) => {
    setVocaList((prev) => [...prev, newVoca]);
  };

  const setEditVocaList = (editVoca: VocaDataType) => {
    setVocaList((prev) => {
      return prev.map((voca: VocaDataType) =>
        voca.vocabId !== editVoca.vocabId
          ? voca
          : {
              ...voca,
              meaning: editVoca.meaning,
              word: editVoca.word,
            }
      );
    });
  };

  if (vocaList.length === 0) {
    return (
      <Empty title="등록된 단어가 없어요">
        <TbVocabulary className={styles.icon} size={'6rem'} />
      </Empty>
    );
  }

  return (
    <>
      {isOpenModal && (
        <VocaModal
          isEditMode={isEditMode}
          onModalClose={() => setIsOpenModal(false)}
          onAddNewVoca={setNewVocaList}
          onAddEditVoca={setEditVocaList}
        />
      )}
      {isAlertOpen && (
        <AlertModal
          labelClose="닫기"
          labelSubmit="삭제"
          onClose={() => {
            setIsAlertOpen(false);
          }}
          onSubmit={deleteVoca}
        >
          단어를 삭제하겠습니까?
        </AlertModal>
      )}
      {/* 단어 리스트 */}
      <ul className={styles.card_list}>
        {vocaList.map((voca: VocaDataType) => (
          <VocaCard
            key={voca.vocabId}
            vocabId={voca.vocabId}
            word={voca.word}
            meaning={voca.meaning}
            onEdit={onEditModalHandler}
            onDelete={onDeleteHandler}
            nation={voca.nation}
          />
        ))}
      </ul>
      <Button
        variant="primary"
        size="md"
        iconBtn
        icon={<AiOutlinePlus />}
        className={styles.button}
        onClick={onAddModalHandler}
      />
      <div ref={sentinelRef}></div>
    </>
  );
};

export default Voca;

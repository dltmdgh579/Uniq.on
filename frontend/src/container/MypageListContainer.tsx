import { QUERY_KEYS } from "@/api/query_key_schema";
import FAVItemCard from "@/components/Card/FAVItemCard";
import NFTItemCard from "@/components/Card/NFTItemCard";
import RSRVItemCard from "@/components/Card/RSRVItemCard";
import Grid from "@/components/Grid";
import { APPLYItem, FAVItem, NFTItem, RSRVItem } from "@/types/api_responses";
import { MypageListType } from "@/types/props";
import React from "react";

const { MY_APPLY_LIST, MY_FAVORITE_LIST, MY_NFT_LIST, MY_RESERVE_LIST } =
  QUERY_KEYS;

const MypageListContainer = ({
  nfts,
  favs,
  rsrvs,
  applys,
  handleModalOpen,
}: // mypageListType,
{
  nfts?: NFTItem[];
  favs?: FAVItem[];
  rsrvs?: RSRVItem[];
  applys?: APPLYItem[];
  handleModalOpen: (type: MypageListType) => void;
  // mypageListType: MypageListType;
}) => {
  return (
    <Grid column="double">
      {nfts?.map((nft: NFTItem) => (
        <NFTItemCard
          key={nft.startupId}
          nftImage={nft.nftImage}
          nftReserveCount={nft.nftReserveCount}
          startupName={nft.startupName}
          nftPrice={nft.nftPrice}
          onClick={() => handleModalOpen(nft)}
          clickable={true}
        />
      ))}
      {favs?.map((fav: FAVItem) => (
        <FAVItemCard
          key={fav.startupId}
          nftReserveCount={fav.nftReserveCount}
          startupId={fav.startupId}
          isFav={fav.isFav}
          nftImage={fav.nftImage}
          startupName={fav.startupName}
          nftPrice={fav.nftPrice}
          dueDate={fav.dueDate}
          nftDescription={fav.nftDescription}
          favItem={fav}
          handleModalOpen={handleModalOpen}
          clickable={true}
        />
      ))}
      {rsrvs?.map((rsrv: RSRVItem) => (
        <RSRVItemCard
          key={rsrv.startupId}
          nftImage={rsrv.nftImage}
          startupName={rsrv.startupName}
          nftPrice={rsrv.nftPrice}
          nftReserveCount={rsrv.nftReserveCount}
          onClick={() => handleModalOpen(rsrv)}
          clickable={true}
        />
      ))}
      {applys?.map((apply: APPLYItem) => (
        <NFTItemCard
          key={apply.startupId}
          nftReserveCount={apply.nftReserveCount}
          nftImage={apply.nftImage}
          startupName={apply.startupName}
          nftPrice={apply.nftPrice}
          onClick={() => handleModalOpen(apply)}
          clickable={true}
        />
      ))}
    </Grid>
  );
};

export default MypageListContainer;

export type DiscountWindowType = 'GLOBAL' | 'RANDOM';

export interface DiscountWindow {
  id: number;
  name: string;
  type: DiscountWindowType;
  /** Fracción — 0.10 = 10 %. */
  rate: number;
  startAt: string;
  endAt: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DiscountWindowRequest {
  name: string;
  type: DiscountWindowType;
  rate: number;
  startAt: string;
  endAt: string;
  active?: boolean;
}

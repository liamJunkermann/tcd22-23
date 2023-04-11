import { DEBUG } from "../constants";

export function storeContext<T>(itemName: string, context: T) {
    try {
      if (typeof window !== "undefined") {
        let storageString = JSON.stringify({
          context,
          exp: Date.now() + 60 * 1000 * 5 /* exp: now + 1000ms * 60s * 5m */,
        });
        // DEBUG && console.log(storageString);
        if (!DEBUG)
          storageString = window.btoa(
            window.unescape(encodeURIComponent(storageString))
          );
        return localStorage.setItem(itemName, storageString);
      }
    } catch (e) {
      console.warn("incorrectly formatted context", itemName, e);
      console.error(e);
    }
  }
  
  export function getContext<T>(
    itemName: string
  ): { context: T; exp: number } | undefined {
    try {
      if (typeof window !== "undefined") {
        let contextString = localStorage.getItem(itemName);
        if (contextString) {
          if (!DEBUG) contextString = window.atob(contextString);
          return JSON.parse(contextString);
        } else return undefined;
      }
    } catch (e) {
      console.warn("incorrectly formatted context", itemName, e);
      return undefined;
    }
  }